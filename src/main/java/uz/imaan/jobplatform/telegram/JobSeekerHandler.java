package uz.imaan.jobplatform.telegram;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobApplicationRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class JobSeekerHandler {

    public enum JobSeekerState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PHONE,
        MAIN_MENU,
        JOB_SEARCH,
        VIEW_JOB_DETAILS,
        ACTIVE_JOBS,
        PROFILE_MENU,
        APPLICATIONS,
        WALLET_MENU,
        SETTINGS_MENU,
        APPLY_COMMENT
    }

    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobStore jobStore;


    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    private final Map<Long, JobSeekerState> states = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> data = new ConcurrentHashMap<>();

    public JobSeekerHandler(JobSeekerProfileRepository jobSeekerProfileRepository,
                            JobApplicationRepository jobApplicationRepository,
                            JobStore jobStore) {
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobStore = jobStore;
    }

    public SendMessage handleJobSeeker(Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText().trim() : "";
        data.putIfAbsent(chatId, new ConcurrentHashMap<>());

        Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
        boolean isRegistered = profileOpt.isPresent();

        JobSeekerState state = states.getOrDefault(chatId, JobSeekerState.NONE);

        // 1. Navigation / Back commands
        if (text.equals("⬅️ Orqaga") || text.equals("❌ Bekor qilish") || text.equals("Asosiy menyu")) {
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "🛠 **Ishchi menyusi**\n\nKerakli bo'limni tanlang:", getMainMenuKeyboard());
        }

        // 2. Initial entry / Registration flow
        if (text.equals("JobSeeker (Ish izlovchi)")) {
            if (isRegistered) {
                states.put(chatId, JobSeekerState.MAIN_MENU);
                return createMessage(chatId, "🛠 **Ishchi menyusi**\n\nKerakli bo'limni tanlang:", getMainMenuKeyboard());
            } else {
                states.put(chatId, JobSeekerState.WAITING_FOR_NAME);
                return createMessage(chatId, "👤 **Ish izlovchi sifatida ro'yxatdan o'tish:**\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`", null);
            }
        }

        if (state == JobSeekerState.WAITING_FOR_NAME && message.hasText()) {
            data.get(chatId).put("fullName", text);
            states.put(chatId, JobSeekerState.WAITING_FOR_PHONE);
            return createMessage(chatId, "📱 **Telefon raqamingizni yuboring:**", getPhoneKeyboard());
        }

        if (state == JobSeekerState.WAITING_FOR_PHONE) {
            String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
            if (!phone.isEmpty()) {
                JobSeekerProfile profile = profileOpt.orElse(new JobSeekerProfile());
                profile.setUserId(chatId);
                profile.setFullName(data.get(chatId).get("fullName"));
                profile.setPhoneNumber(phone);
                jobSeekerProfileRepository.save(profile);

                states.put(chatId, JobSeekerState.MAIN_MENU);
                data.remove(chatId);
                return createMessage(chatId, "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**\n\nIshchi menyusi:", getMainMenuKeyboard());
            }
        }

        // 3. Cover letter (Izoh) yozilganda arizani saqlash VA Ish beruvchiga yuborish
        if (state == JobSeekerState.APPLY_COMMENT && message.hasText()) {
            String coverLetterText = text;
            int jobIndex = Integer.parseInt(data.get(chatId).getOrDefault("selectedJobIndex", "0"));

            JobSeekerProfile profile = profileOpt.orElseGet(() -> {
                JobSeekerProfile p = new JobSeekerProfile();
                p.setUserId(chatId);
                return jobSeekerProfileRepository.save(p);
            });

            // Arizani saqlaymiz
            JobApplication application = new JobApplication();
            application.setJobId((long) jobIndex);
            application.setJobSeekerId(profile.getId());
            application.setCoverLetter(coverLetterText);
            application.setStatus(JobApplication.ApplicationStatus.PENDING);
            jobApplicationRepository.save(application);

            // --- ISH BERUVCHIGA TELEGRAMDAN BILDIRISHNOMA YUBORISH ---
            List<JobVacancy> allVacancies = jobStore.getAllVacancies();
            if (jobIndex >= 0 && jobIndex < allVacancies.size()) {
                JobVacancy selectedJob = allVacancies.get(jobIndex);
                Long employerChatId = selectedJob.getEmployerChatId();

                if (employerChatId != null) {
                    String notifyText = String.format(
                            "📩 **Vakansiyangizga yangi ariza keldi!**\n\n" +
                                    "📌 **Vakansiya:** %s\n" +
                                    "👤 **Nomzod:** %s\n" +
                                    "📞 **Tel:** %s\n" +
                                    "✍️ **Izoh:** %s",
                            selectedJob.getTitle(),
                            profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                            profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                            coverLetterText
                    );

                    SendMessage notifyMsg = new SendMessage(employerChatId.toString(), notifyText);
                    notifyMsg.setParseMode("Markdown");

                    try {
                        // ApplicationContext orqali Telegram botni chaqirib xabar yuboramiz (Sikl buziladi)
                        Telegram telegramBot = applicationContext.getBean(Telegram.class);
                        telegramBot.execute(notifyMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "✅ **Ariza yuborildi✓**\n\nArizangiz ish beruvchiga ko'rib chiqish uchun muvaffaqiyatli yetkazildi!", getMainMenuKeyboard());
        }

        // 4. Main menu handler
        switch (text) {
            case "🔍 Ish qidirish":
                states.put(chatId, JobSeekerState.JOB_SEARCH);
                return createMessage(chatId, "📂 **Kategoriyani tanlang:**", getCategoryKeyboard());

            case "⚡ Faol ishlarim":
                states.put(chatId, JobSeekerState.ACTIVE_JOBS);
                return createMessage(chatId, "⚡ **Faol ishlarim bo'limi:**", getActiveJobsKeyboard());

            case "👤 Profilim":
                states.put(chatId, JobSeekerState.PROFILE_MENU);
                JobSeekerProfile profile = profileOpt.orElse(new JobSeekerProfile());
                double rating = profile.getRating() != null ? profile.getRating() : 0.0;
                String info = String.format("👤 **Profil ma'lumotlari:**\n\n📌 **F.I.O:** %s\n📞 **Tel:** %s\n⭐ **Reyting:** %.1f\n💼 **Kasb:** %s",
                        profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                        profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                        rating,
                        profile.getProfession() != null ? profile.getProfession() : "Ko'rsatilmagan");
                return createMessage(chatId, info, getProfileKeyboard());

            case "📂 Arizalar":
            case "📁 Arizalar":
                states.put(chatId, JobSeekerState.APPLICATIONS);
                return handleShowApplications(chatId, profileOpt);

            case "💳 Hamyon":
                states.put(chatId, JobSeekerState.WALLET_MENU);
                return createMessage(chatId, "💳 **Hamyon va To'lovlar:**\n\n💰 **Hisob balansi:** 0 so'm", getWalletKeyboard());

            case "⚙️ Sozlamalar":
                states.put(chatId, JobSeekerState.SETTINGS_MENU);
                return createMessage(chatId, "⚙️ **Sozlamalar bo'limi:**", getSettingsKeyboard());
        }

        // 5. ISH QIDIRISH (Kategoriya bo'yicha filterlash to'g'rilandi)
        if (state == JobSeekerState.JOB_SEARCH) {
            List<JobVacancy> vacancies;

            if (text.contains("Barcha vakansiyalar")) {
                vacancies = jobStore.getAllVacancies();
            } else {
                // Emojilarni tozalab, matn bo'yicha qidiramiz
                String cleanKeyword = text.replaceAll("[^a-zA-Z0-9& ]", "").trim();

                vacancies = jobStore.getAllVacancies().stream()
                        .filter(v -> v.getCategory() != null &&
                                (v.getCategory().toLowerCase().contains(cleanKeyword.toLowerCase()) ||
                                        cleanKeyword.toLowerCase().contains(v.getCategory().toLowerCase())))
                        .toList();
            }

            if (vacancies == null || vacancies.isEmpty()) {
                return createMessage(chatId, "🔍 Ushbu kategoriya bo'yicha hozircha vakansiyalar mavjud emas.", getCategoryKeyboard());
            }

            states.put(chatId, JobSeekerState.VIEW_JOB_DETAILS);

            ReplyKeyboardMarkup jobsKeyboard = new ReplyKeyboardMarkup();
            jobsKeyboard.setResizeKeyboard(true);
            List<KeyboardRow> rows = new ArrayList<>();

            for (int i = 0; i < vacancies.size(); i++) {
                JobVacancy v = vacancies.get(i);
                KeyboardRow row = new KeyboardRow();
                row.add("📌 [" + i + "] " + v.getTitle());
                rows.add(row);
            }
            KeyboardRow backRow = new KeyboardRow();
            backRow.add("⬅️ Orqaga");
            rows.add(backRow);
            jobsKeyboard.setKeyboard(rows);

            return createMessage(chatId, "💼 **Topilgan vakansiyalar:**\n\nBatafsil ko'rish uchun tanlang:", jobsKeyboard);
        }

        // 6. Vakansiya tanlanganda
        if (state == JobSeekerState.VIEW_JOB_DETAILS) {
            if (text.startsWith("📌 [")) {
                try {
                    int index = Integer.parseInt(text.substring(text.indexOf("[") + 1, text.indexOf("]")));
                    List<JobVacancy> allVacancies = jobStore.getAllVacancies();

                    if (index >= 0 && index < allVacancies.size()) {
                        JobVacancy selectedVacancy = allVacancies.get(index);
                        data.get(chatId).put("selectedJobIndex", String.valueOf(index));
                        data.get(chatId).put("selectedJobTitle", selectedVacancy.getTitle());

                        String detail = String.format(
                                "📄 **Ish ma'lumotlari:**\n\n" +
                                        "🔹 **Nomi:** %s\n" +
                                        "📂 **Kategoriya:** %s\n" +
                                        "⏱ **Bandlik turi:** %s\n" +
                                        "💰 **Maosh:** %s",
                                selectedVacancy.getTitle(),
                                selectedVacancy.getCategory() != null ? selectedVacancy.getCategory() : "Ko'rsatilmagan",
                                selectedVacancy.getType() != null ? selectedVacancy.getType() : "To'liq kun",
                                selectedVacancy.getSalary() != null ? selectedVacancy.getSalary() : "Kelishilgan holda"
                        );
                        return createMessage(chatId, detail, getJobActionKeyboard());
                    }
                } catch (Exception e) {
                    return createMessage(chatId, "Vakansiya ma'lumotlarini yuklashda xatolik bo'ldi.", getMainMenuKeyboard());
                }
            }

            if (text.equals("📝 Ariza berish")) {
                states.put(chatId, JobSeekerState.APPLY_COMMENT);
                return createMessage(chatId, "✍️ **Cover letter (Izoh) yozing:**\n\nO'zingiz va tajribangiz haqida qisqacha ma'lumot qoldiring:", getCancelKeyboard());
            }
        }

        // 7. Ichki menyular
        if (state == JobSeekerState.WALLET_MENU) {
            if (text.equals("💳 Bank kartasi qo'shish")) {
                return createMessage(chatId, "💳 Karta raqamingizni kiriting (16 xona):", getSubBackKeyboard());
            } else if (text.equals("💰 Hisob balansi")) {
                return createMessage(chatId, "💰 **Joriy balansingiz:** 0 so'm", getWalletKeyboard());
            } else if (text.equals("💸 Pul yechish")) {
                return createMessage(chatId, "⚠️ Pul yechish uchun minimal summa: 50,000 so'm.", getWalletKeyboard());
            }
        }

        if (state == JobSeekerState.PROFILE_MENU) {
            if (text.equals("✏️ Tahrirlash")) {
                return createMessage(chatId, "Ismingizni o'zgartirish uchun yangi ism kiriting:", getSubBackKeyboard());
            }
        }

        return createMessage(chatId, "Iltimos, tugmalardan birini tanlang.", getMainMenuKeyboard());
    }

    private SendMessage handleShowApplications(Long chatId, Optional<JobSeekerProfile> profileOpt) {
        if (profileOpt.isEmpty()) {
            return createMessage(chatId, "📂 Arizalaringizni ko'rish uchun avval ro'yxatdan o'ting.", getMainMenuKeyboard());
        }

        Long jobSeekerId = profileOpt.get().getId();
        List<JobApplication> myApps = jobApplicationRepository.findByJobSeekerId(jobSeekerId);

        if (myApps == null || myApps.isEmpty()) {
            return createMessage(chatId, "📂 Siz hali hech qanday vakansiyaga ariza topshirmagansiz.", getSubBackKeyboard());
        }

        StringBuilder sb = new StringBuilder("📋 **Siz yuborgan arizalar:**\n\n");
        List<JobVacancy> allVacancies = jobStore.getAllVacancies();

        for (JobApplication app : myApps) {
            int index = app.getJobId().intValue();
            String jobTitle = (index >= 0 && index < allVacancies.size())
                    ? allVacancies.get(index).getTitle()
                    : "Vakansiya #" + app.getJobId();

            String statusStr = switch (app.getStatus()) {
                case PENDING -> "⏳ Ko'rib chiqilmoqda";
                case ACCEPTED -> "✅ Qabul qilindi";
                case REJECTED -> "❌ Rad etildi";
                case CANCELLED -> "🚫 Bekor qilingan";
            };

            sb.append("📌 **Vakansiya:** ").append(jobTitle).append("\n")
                    .append("✍️ **Cover letter:** ").append(app.getCoverLetter() != null ? app.getCoverLetter() : "Mavjud emas").append("\n")
                    .append("📊 **Holat:** ").append(statusStr).append("\n")
                    .append("───────────────\n");
        }
        return createMessage(chatId, sb.toString(), getSubBackKeyboard());
    }

    // --- KEYBOARDS ---

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🔍 Ish qidirish");
        row1.add("⚡ Faol ishlarim");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("👤 Profilim");
        row2.add("📂 Arizalar");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("💳 Hamyon");
        row3.add("⚙️ Sozlamalar");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("Asosiy menyu");

        markup.setKeyboard(List.of(row1, row2, row3, row4));
        return markup;
    }

    private ReplyKeyboardMarkup getJobActionKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📝 Ariza berish");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    private ReplyKeyboardMarkup getCancelKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("❌ Bekor qilish");

        markup.setKeyboard(List.of(row));
        return markup;
    }

    private ReplyKeyboardMarkup getProfileKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📊 Ma'lumotlar");
        row1.add("📁 Portfolio");
        row1.add("⭐ Reyting");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🖼 Rasm");
        row2.add("💼 Kasb");
        row2.add("✏️ Tahrirlash");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    private ReplyKeyboardMarkup getWalletKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("💳 Bank kartasi qo'shish");
        row1.add("💰 Hisob balansi");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("💳 To'lov usullari");
        row2.add("📜 To'lov tarixi");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("💸 Pul yechish");
        row3.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    private ReplyKeyboardMarkup getSettingsKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🌐 Til");
        row1.add("🔒 Maxfiylik");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🔔 Bildirishnoma");
        row2.add("❓ Yordam");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    private ReplyKeyboardMarkup getActiveJobsKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📌 Joriy ishlar");
        row1.add("📋 Topshiriqlar");
        row1.add("📝 Vazifalar");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    private ReplyKeyboardMarkup getCategoryKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("💻 IT & Dasturlash");
        row1.add("🎨 Dizayn");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🌐 Barcha vakansiyalar");
        row2.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    private ReplyKeyboardMarkup getSubBackKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row));
        return markup;
    }

    private ReplyKeyboardMarkup getPhoneKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("📱 Telefon raqamni yuborish");
        button.setRequestContact(true);
        row.add(button);

        markup.setKeyboard(List.of(row));
        return markup;
    }

    private SendMessage createMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        if (keyboard != null) message.setReplyMarkup(keyboard);
        return message;
    }
}
