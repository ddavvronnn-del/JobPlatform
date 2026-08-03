package uz.imaan.jobplatform.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;


import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import uz.imaan.jobplatform.jobseeker.dto.BankCardRequest;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobApplicationRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;
import uz.imaan.jobplatform.jobseeker.service.WalletService;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class JobSeekerHandler {

    // ============================================
    // ENUM: HOLATLAR
    // ============================================
    public enum JobSeekerState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PASSPORT,
        WAITING_FOR_PHONE,
        MAIN_MENU,
        JOB_SEARCH,
        VIEW_JOB_DETAILS,
        ACTIVE_JOBS,
        PROFILE_MENU,
        APPLICATIONS,
        WALLET_MENU,
        SETTINGS_MENU,
        APPLY_COMMENT,
        WAITING_FOR_PROFESSION,
        WAITING_FOR_EDIT_NAME,
        WAITING_FOR_CARD_NUMBER,
        WAITING_FOR_CARD_EXPIRY,
        WAITING_FOR_CARD_HOLDER
    }

    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobStore jobStore;
    private final WalletService walletService;

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<Long, JobSeekerState> states = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> data = new ConcurrentHashMap<>();

    // ============================================
    // KONSTRUKTOR
    // ============================================
    public JobSeekerHandler(JobSeekerProfileRepository jobSeekerProfileRepository,
                            JobApplicationRepository jobApplicationRepository,
                            JobStore jobStore,
                            WalletService walletService) {
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobStore = jobStore;
        this.walletService = walletService;
    }

    // ============================================
    // ASOSIY HANDLE METODI
    // ============================================
    public SendMessage handleJobSeeker(Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText().trim() : "";
        data.putIfAbsent(chatId, new ConcurrentHashMap<>());

        Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
        boolean isRegistered = profileOpt.isPresent();

        JobSeekerState state = states.getOrDefault(chatId, JobSeekerState.NONE);

        // ============================================
        // 1. NAVIGATION / BACK COMMANDS
        // ============================================
        if (text.equals("⬅️ Orqaga") || text.equals("❌ Bekor qilish") || text.equals("Asosiy menyu")) {
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "🛠 **Ishchi menyusi**\n\nKerakli bo'limni tanlang:", getMainMenuKeyboard());
        }

        // ============================================
        // 2. KARTA QO'SHISH HOLATLARI (YANGI)
        // ============================================

        // 2.1 KARTA RAQAMI
        if (state == JobSeekerState.WAITING_FOR_CARD_NUMBER) {
            return handleCardNumber(chatId, text);
        }

        // 2.2 KARTA MUDDATI
        if (state == JobSeekerState.WAITING_FOR_CARD_EXPIRY) {
            return handleCardExpiry(chatId, text);
        }

        // 2.3 KARTA EGASI
        if (state == JobSeekerState.WAITING_FOR_CARD_HOLDER) {
            return handleCardHolder(chatId, text, profileOpt);
        }

        // ============================================
        // 3. RO'YXATDAN O'TISH
        // ============================================
        if (text.equals("JobSeeker (Ish izlovchi)")) {
            if (isRegistered) {
                states.put(chatId, JobSeekerState.MAIN_MENU);
                return createMessage(chatId, "🛠 **Ishchi menyusi**\n\nKerakli bo'limni tanlang:", getMainMenuKeyboard());
            } else {
                states.put(chatId, JobSeekerState.WAITING_FOR_NAME);
                return createMessage(chatId, "👤 **Ish izlovchi sifatida ro'yxatdan o'tish:**" +
                        "\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`", null);
            }
        }

        // Step 1: Ism-familiya
        if (state == JobSeekerState.WAITING_FOR_NAME && message.hasText()) {
            data.get(chatId).put("fullName", text);
            states.put(chatId, JobSeekerState.WAITING_FOR_PASSPORT);
            return createMessage(chatId, "🪪 **Pasport seriya va raqamingizni kiriting:**" +
                    "\n\n💡 *Misol:* `AA1234567`", null);
        }

        // Step 2: Pasport
        if (state == JobSeekerState.WAITING_FOR_PASSPORT && message.hasText()) {
            data.get(chatId).put("passport", text);
            states.put(chatId, JobSeekerState.WAITING_FOR_PHONE);
            return createMessage(chatId, "📱 **Telefon raqamingizni yuboring:**", getPhoneKeyboard());
        }

        // Step 3: Telefon
        if (state == JobSeekerState.WAITING_FOR_PHONE) {
            String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
            if (!phone.isEmpty()) {
                JobSeekerProfile profile = profileOpt.orElse(new JobSeekerProfile());
                profile.setUserId(chatId);
                profile.setFullName(data.get(chatId).get("fullName"));
                profile.setPassportNumber(data.get(chatId).get("passport"));
                profile.setPhoneNumber(phone);
                jobSeekerProfileRepository.save(profile);

                states.put(chatId, JobSeekerState.MAIN_MENU);
                data.remove(chatId);
                return createMessage(chatId, "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**" +
                        "\n\nIshchi menyusi:", getMainMenuKeyboard());
            }
        }

        // ============================================
        // 4. KASBNI SAQLASH
        // ============================================
        if (state == JobSeekerState.WAITING_FOR_PROFESSION && message.hasText()) {
            JobSeekerProfile profile = profileOpt.orElseGet(() -> {
                JobSeekerProfile p = new JobSeekerProfile();
                p.setUserId(chatId);
                return p;
            });
            profile.setProfession(text);
            jobSeekerProfileRepository.save(profile);

            states.put(chatId, JobSeekerState.PROFILE_MENU);
            return createMessage(chatId, "✅ **Kasbingiz muvaffaqiyatli saqlandi!**" +
                    "\n\n💼 Yangi kasb: `" + text + "`", getProfileKeyboard());
        }

        // ============================================
        // 5. ISMNI TAHRIRLASH
        // ============================================
        if (state == JobSeekerState.WAITING_FOR_EDIT_NAME && message.hasText()) {
            JobSeekerProfile profile = profileOpt.orElseGet(() -> {
                JobSeekerProfile p = new JobSeekerProfile();
                p.setUserId(chatId);
                return p;
            });
            profile.setFullName(text);
            jobSeekerProfileRepository.save(profile);

            states.put(chatId, JobSeekerState.PROFILE_MENU);
            return createMessage(chatId, "✅ **Ism-familiyangiz muvaffaqiyatli yangilandi!**" +
                    "\n\n👤 Yangi F.I.O: `" + text + "`", getProfileKeyboard());
        }

        // ============================================
        // 6. COVER LETTER (Ariza izohi)
        // ============================================
        if (state == JobSeekerState.APPLY_COMMENT && message.hasText()) {
            String coverLetterText = text;
            int jobIndex = Integer.parseInt(data.get(chatId).getOrDefault("selectedJobIndex", "0"));

            JobSeekerProfile profile = profileOpt.orElseGet(() -> {
                JobSeekerProfile p = new JobSeekerProfile();
                p.setUserId(chatId);
                return jobSeekerProfileRepository.save(p);
            });

            JobApplication application = new JobApplication();
            application.setJobId((long) jobIndex);
            application.setJobSeekerId(profile.getId());
            application.setCoverLetter(coverLetterText);
            application.setStatus(JobApplication.ApplicationStatus.PENDING);
            jobApplicationRepository.save(application);

            List<JobVacancy> allVacancies = jobStore.getAllVacancies();
            if (jobIndex >= 0 && jobIndex < allVacancies.size()) {
                JobVacancy selectedJob = allVacancies.get(jobIndex);
                Long employerChatId = selectedJob.getEmployerChatId();

                if (employerChatId != null) {
                    String notifyText = String.format(
                            "📩 **Vakansiyangizga yangi ariza keldi!**\n\n" +
                                    "📌 **Vakansiya:** %s\n" +
                                    "👤 **Nomzod:** %s\n" +
                                    "🪪 **Pasport:** %s\n" +
                                    "📞 **Tel:** %s\n" +
                                    "✍️ **Izoh:** %s",
                            selectedJob.getTitle(),
                            profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                            profile.getPassportNumber() != null ? profile.getPassportNumber() : "Kiritilmagan",
                            profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                            coverLetterText
                    );

                    SendMessage notifyMsg = new SendMessage(employerChatId.toString(), notifyText);
                    notifyMsg.setParseMode("Markdown");
                    try {
                        Telegram telegramBot = applicationContext.getBean(Telegram.class);
                        telegramBot.execute(notifyMsg);
                    } catch (Exception e) {
                        log.error("❌ Xatolik: {}", e.getMessage());
                    }
                }
            }

            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "✅ **Ariza yuborildi✓**" +
                    "\n\nArizangiz ish beruvchiga ko'rib chiqish uchun muvaffaqiyatli yetkazildi!", getMainMenuKeyboard());
        }

        // ============================================
        // 7. MAIN MENU
        // ============================================
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
                String info = String.format(
                        "👤 **Profil ma'lumotlari:**\n\n" +
                                "📌 **F.I.O:** %s\n" +
                                "🪪 **Pasport:** %s\n" +
                                "📞 **Tel:** %s\n" +
                                "⭐ **Reyting:** %.1f\n" +
                                "💼 **Kasb:** %s",
                        profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                        profile.getPassportNumber() != null ? profile.getPassportNumber() : "Kiritilmagan",
                        profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                        rating,
                        profile.getProfession() != null ? profile.getProfession() : "Ko'rsatilmagan"
                );
                return createMessage(chatId, info, getProfileKeyboard());

            case "📂 Arizalar":
            case "📁 Arizalar":
                states.put(chatId, JobSeekerState.APPLICATIONS);
                return handleShowApplications(chatId, profileOpt);

            case "💳 Hamyon":
                states.put(chatId, JobSeekerState.WALLET_MENU);
                return showWallet(chatId);

            case "⚙️ Sozlamalar":
                states.put(chatId, JobSeekerState.SETTINGS_MENU);
                return createMessage(chatId, "⚙️ **Sozlamalar bo'limi:**", getSettingsKeyboard());
        }

        // ============================================
        // 8. ISH QIDIRISH
        // ============================================
        if (state == JobSeekerState.JOB_SEARCH) {
            List<JobVacancy> vacancies;

            if (text.contains("Barcha vakansiyalar")) {
                vacancies = jobStore.getAllVacancies();
            } else {
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
            List<KeyboardRow> rows = new java.util.ArrayList<>();

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

        // ============================================
        // 9. VAKANSIYA TANLANGANDA
        // ============================================
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
                return createMessage(chatId, "✍️ **Cover letter (Izoh) yozing:**" +
                        "\n\nO'zingiz va tajribangiz haqida qisqacha ma'lumot qoldiring:", getCancelKeyboard());
            }
        }

        // ============================================
        // 10. PROFIL MENYUSI
        // ============================================
        if (state == JobSeekerState.PROFILE_MENU) {
            JobSeekerProfile profile = profileOpt.orElse(new JobSeekerProfile());
            double r = profile.getRating() != null ? profile.getRating() : 0.0;

            if (text.contains("Ma'lumotlar")) {
                String profileInfo = String.format(
                        "👤 **Profil ma'lumotlari:**\n\n" +
                                "📌 **F.I.O:** %s\n" +
                                "🪪 **Pasport:** %s\n" +
                                "📞 **Tel:** %s\n" +
                                "⭐ **Reyting:** %.1f\n" +
                                "💼 **Kasb:** %s",
                        profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                        profile.getPassportNumber() != null ? profile.getPassportNumber() : "Kiritilmagan",
                        profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                        r,
                        profile.getProfession() != null ? profile.getProfession() : "Ko'rsatilmagan"
                );
                return createMessage(chatId, profileInfo, getProfileKeyboard());
            }

            if (text.contains("Portfolio")) {
                return createMessage(chatId, "📁 **Portfolio bo'limi:**" +
                        "\n\nHozircha portfolio yuklanmagan. Loyihalaringiz havolasini (link) yuborishingiz mumkin:", getSubBackKeyboard());
            }

            if (text.contains("Reyting")) {
                return createMessage(chatId, String.format("⭐ **Sizning joriy reytingingiz:** %.1f / 5.0" +
                        "\n\nBajarilgan ishlar va ish beruvchilar bahosi asosida shakllanadi.", r), getProfileKeyboard());
            }

            if (text.contains("Rasm")) {
                return createMessage(chatId, "🖼 **Profil rasmi:**" +
                        "\n\nProfil rasmingizni yangilash uchun botga rasm yuboring:", getSubBackKeyboard());
            }

            if (text.contains("Kasb")) {
                states.put(chatId, JobSeekerState.WAITING_FOR_PROFESSION);
                String profession = profile.getProfession() != null ? profile.getProfession() : "Ko'rsatilmagan";
                return createMessage(chatId, String.format("💼 **Joriy kasbingiz:** %s" +
                        "\n\nKasbingizni o'zgartirish uchun yangi kasb nomini kiriting (Masalan: Java Developer):",
                        profession), getSubBackKeyboard());
            }

            if (text.contains("Tahrirlash")) {
                states.put(chatId, JobSeekerState.WAITING_FOR_EDIT_NAME);
                return createMessage(chatId, "✏️ **Profilni tahrirlash:**" +
                        "\n\nYangi ism va familiyangizni kiriting:", getSubBackKeyboard());
            }
        }

        // ============================================
        // 11. HAMYON MENYUSI
        // ============================================
        if (state == JobSeekerState.WALLET_MENU) {
            if (text.contains("Bank kartasi qo'shish") || text.contains("💳 Bank kartasi")) {
                states.put(chatId, JobSeekerState.WAITING_FOR_CARD_NUMBER);
                return createMessage(chatId, "💳 Karta raqamingizni kiriting (16 xona):", getCancelKeyboard());
            }
            if (text.contains("Hisob balansi") || text.contains("💰 Hisob balansi")) {
                return showWallet(chatId);
            }
            if (text.contains("Pul yechish")) {
                return createMessage(chatId, "⚠️ Pul yechish uchun minimal summa: 50,000 so'm.", getWalletKeyboard());
            }
            if (text.contains("To'lov usullari")) {
                return createMessage(chatId, "💳 **Mavjud to'lov usullari:**\n\n- Click\n- Payme\n- Uzum Bank", getWalletKeyboard());
            }
            if (text.contains("To'lov tarixi")) {
                return createMessage(chatId, "📜 **To'lovlar tarixi:**\n\nHozircha amaliyotlar mavjud emas.", getWalletKeyboard());
            }
        }

        // ============================================
        // 12. SOZLAMALAR MENYUSI
        // ============================================
        if (state == JobSeekerState.SETTINGS_MENU) {
            if (text.contains("Til")) {
                return createMessage(chatId, "🌐 **Tilni tanlang:**\n\n🇺🇿 O'zbek tili (Aktiv)", getSettingsKeyboard());
            }
            if (text.contains("Maxfiylik")) {
                return createMessage(chatId, "🔒 **Maxfiylik sozlamalari:**" +
                        "\n\nSizning ma'lumotlaringiz xavfsiz saqlanadi.", getSettingsKeyboard());
            }
            if (text.contains("Bildirishnoma")) {
                return createMessage(chatId, "🔔 **Bildirishnomalar:** Yoniq ✅", getSettingsKeyboard());
            }
            if (text.contains("Yordam")) {
                return createMessage(chatId, "❓ **Yordam markazi:**" +
                        "\n\nMuammo yuzaga kelsa, admin bilan bog'laning.", getSettingsKeyboard());
            }
        }

        // ============================================
        // 13. FAOL ISHLAR
        // ============================================
        if (state == JobSeekerState.ACTIVE_JOBS) {
            if (text.contains("Joriy ishlar") || text.contains("Topshiriqlar") || text.contains("Vazifalar")) {
                return createMessage(chatId, "📋 Hozircha faol ishlaringiz mavjud emas.", getActiveJobsKeyboard());
            }
        }

        return createMessage(chatId, "Iltimos, tugmalardan birini tanlang.", getMainMenuKeyboard());
    }

    // ============================================
    // KARTA QO'SHISH METODLARI (YANGI)
    // ============================================

    // 1. KARTA RAQAMI
    private SendMessage handleCardNumber(Long chatId, String text) {
        // 1. 16 ta raqamdan iboratligini tekshirish
        if (!text.matches("\\d{16}")) {
            return createMessage(chatId, "❌ Karta raqami 16 ta raqamdan iborat bo'lishi kerak!\nQaytadan kiriting:",
                    getCancelKeyboard());
        }

        // 2. Karta raqamini saqlash
        data.get(chatId).put("cardNumber", text);
        log.info("✅ Karta raqami qabul qilindi: {}", text);

        // 3. KEYINGI QADAM: MUDDAT SO'RASH
        states.put(chatId, JobSeekerState.WAITING_FOR_CARD_EXPIRY);
        return createMessage(chatId, "📅 Karta amal qilish muddatini kiriting (MM/YY):\nMasalan: 12/26", getCancelKeyboard());
    }

    // 2. KARTA MUDDATI
    private SendMessage handleCardExpiry(Long chatId, String text) {
        // 1. Formatni tekshirish (MM/YY)
        if (!text.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            return createMessage(chatId, "❌ Noto'g'ri format! Format: MM/YY\nMasalan: 12/26", getCancelKeyboard());
        }

        // 2. Muddat o'tmaganligini tekshirish
        String[] parts = text.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;

        LocalDate now = LocalDate.now();
        if (year < now.getYear() || (year == now.getYear() && month < now.getMonthValue())) {
            return createMessage(chatId, "❌ Karta muddati o'tgan! Boshqa kartani kiriting:", getCancelKeyboard());
        }

        // 3. Karta muddatini saqlash
        data.get(chatId).put("cardExpiry", text);
        log.info("✅ Karta muddati qabul qilindi: {}", text);

        // 4. KEYINGI QADAM: KARTA EGASI SO'RASH
        states.put(chatId, JobSeekerState.WAITING_FOR_CARD_HOLDER);
        return createMessage(chatId, "👤 Karta egasi ismini kiriting:\nMasalan: ALI VALIYEV", getCancelKeyboard());
    }

    // 3. KARTA EGASI
    private SendMessage handleCardHolder(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        // 1. Karta egasi ismini tekshirish
        if (text.length() < 3) {
            return createMessage(chatId, "❌ Karta egasi ismi juda qisqa! Qaytadan kiriting:", getCancelKeyboard());
        }

        // 2. User ID ni olish
        Long userId = chatId;
        if (profileOpt.isPresent()) {
            userId = profileOpt.get().getUserId();
        } else {
            return createMessage(chatId, "❌ Siz hali ro'yxatdan o'tmagansiz! Iltimos, /start bosing.", getMainMenuKeyboard());
        }

        // 3. Kartani saqlash
        String cardNumber = data.get(chatId).get("cardNumber");
        String expiryDate = data.get(chatId).get("cardExpiry");

        try {
            BankCardRequest request = new BankCardRequest();
            request.setCardNumber(cardNumber);
            request.setExpireDate(expiryDate);
            request.setCardHolderName(text.toUpperCase());

            walletService.addBankCard(userId, request);

            // Tozalash
            states.put(chatId, JobSeekerState.WALLET_MENU);
            data.get(chatId).remove("cardNumber");
            data.get(chatId).remove("cardExpiry");

            return createMessage(chatId, "✅ Karta muvaffaqiyatli qo'shildi!\n\n" +
                    "💳 Karta: " + cardNumber + "\n" +
                    "📅 Muddati: " + expiryDate + "\n" +
                    "👤 Egasi: " + text.toUpperCase(), getWalletKeyboard());

        } catch (Exception e) {
            log.error("❌ Karta qo'shishda xatolik: {}", e.getMessage());
            states.put(chatId, JobSeekerState.WALLET_MENU);
            return createMessage(chatId, "❌ Karta qo'shishda xatolik: " + e.getMessage(), getWalletKeyboard());
        }
    }

    // ============================================
    // HAMYON
    // ============================================
    private SendMessage showWallet(Long chatId) {
        try {
            // Balansni olish
            Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
            if (profileOpt.isEmpty()) {
                return createMessage(chatId, "❌ Siz hali ro'yxatdan o'tmagansiz!", getMainMenuKeyboard());
            }

            JobSeekerProfile profile = profileOpt.get();
            String balance = profile.getWalletBalance() != null ? profile.getWalletBalance().toString() : "0";

            String message = "💳 **Hamyon va To'lovlar:**\n\n" +
                    "💰 **Hisob balansi:** " + balance + " so'm\n\n" +
                    "📌 Quyidagi tugmalardan foydalaning:";

            return createMessage(chatId, message, getWalletKeyboard());

        } catch (Exception e) {
            log.error("❌ Hamyonni ko'rsatishda xatolik: {}", e.getMessage());
            return createMessage(chatId, "❌ Xatolik yuz berdi!", getMainMenuKeyboard());
        }
    }

    // ============================================
    // ARIZALAR
    // ============================================
    private SendMessage handleShowApplications(Long chatId, Optional<JobSeekerProfile> profileOpt) {
        if (profileOpt.isEmpty()) {
            return createMessage(chatId, "📂 Arizalaringizni ko'rish uchun avval ro'yxatdan o'ting.", getMainMenuKeyboard());
        }

        Long jobSeekerId = profileOpt.get().getId();
        List<JobApplication> myApps = jobApplicationRepository.findByJobSeekerId(jobSeekerId);

        if (myApps == null || myApps.isEmpty()) {
            return createMessage(chatId, "📂 Siz hali hech qanday vakansiyaga ariza topshirmagansiz.",
                    getSubBackKeyboard());
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
                    .append("✍️ **Cover letter:** ").append(app.getCoverLetter() != null ?
                            app.getCoverLetter() : "Mavjud emas").append("\n")
                    .append("📊 **Holat:** ").append(statusStr).append("\n")
                    .append("───────────────\n");
        }
        return createMessage(chatId, sb.toString(), getSubBackKeyboard());
    }

    // ============================================
    // KEYBOARDS
    // ============================================

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
        row3.add("⬅Orqaga");

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

    // ============================================
    // YORDAMCHI METOD
    // ============================================
    private SendMessage createMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        if (keyboard != null) message.setReplyMarkup(keyboard);
        return message;
    }



}