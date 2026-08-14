package uz.imaan.jobplatform.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobApplicationRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;
import uz.imaan.jobplatform.telegram.JobSeekerHandler.interfaces.JobSeekerHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Telegram extends TelegramLongPollingBot {

    public enum UserRole {
        NONE,
        EMPLOYER,
        JOB_SEEKER
    }

    private final JobSeekerHandler jobSeekerHandler;
    private final EmployerHandler employerHandler;
    private final JobStore jobStore;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;

    private final Map<Long, UserRole> userRoles = new ConcurrentHashMap<>();

    @Autowired
    public Telegram(
            JobSeekerHandler jobSeekerHandler,
            EmployerHandler employerHandler,
            JobStore jobStore,
            JobSeekerProfileRepository jobSeekerProfileRepository,
            JobApplicationRepository jobApplicationRepository,
            @Value("${telegram.bot.token}") String botToken) {
        super(botToken);
        this.jobSeekerHandler = jobSeekerHandler;
        this.employerHandler = employerHandler;
        this.jobStore = jobStore;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        log.info("✅ Telegram bot ishga tushdi!");
    }

    @Override
    public String getBotUsername() {
        return "@JobPlatformUzBot";
    }

    @Override
    public void onUpdateReceived(Update update) {

        // ============================================
        // 1. CALLBACK QUERY (INLINE TUGMALAR)
        // ============================================
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            Long chatId = callbackQuery.getMessage().getChatId();

            log.info("📩 Callback: chatId={}, data={}", chatId, callbackData);

            try {
                // TIL TANLASH
                if (callbackData.startsWith("lang_")) {
                    String language = callbackData.replace("lang_", "");
                    jobSeekerHandler.updateLanguage(chatId, language);

                    // Yangilangan profilni olish (hali mavjud bo'lmasligi mumkin)
                    Optional<JobSeekerProfile> updatedProfile = jobSeekerProfileRepository.findByUserId(chatId);

                    String responseText = switch (language) {
                        case "uz" -> "✅ Til O'zbek tiliga o'zgartirildi!";
                        case "ru" -> "✅ Язык изменен на Русский!";
                        case "en" -> "✅ Language changed to English!";
                        default -> "❌ Noto'g'ri tanlov!";
                    };

                    SendMessage response = new SendMessage();
                    response.setChatId(chatId.toString());
                    response.setText(responseText + "\n\n" + "Hush kelibsiz! Rolingizni tanlang:");
                    response.setReplyMarkup(getRoleSelectionKeyboard(updatedProfile));
                    executeMessage(response);
                    return;
                }

                // KATEGORIYA TANLASH
                if (callbackData.startsWith("category_")) {
                    String categoryKey = callbackData.replace("category_", "");
                    String categoryName = getCategoryName(categoryKey);

                    List<JobVacancy> allVacancies = jobStore.getAllVacancies();
                    List<JobVacancy> vacancies;
                    if (categoryKey.equals("all")) {
                        vacancies = allVacancies;
                    } else {
                        vacancies = allVacancies.stream()
                                .filter(v -> v.getCategory() != null &&
                                        v.getCategory().toLowerCase().contains(categoryName.toLowerCase()))
                                .toList();
                    }

                    if (vacancies.isEmpty()) {
                        String msg = "🔍 Ushbu kategoriya bo'yicha hozircha vakansiyalar mavjud emas.";
                        SendMessage response = new SendMessage();
                        response.setChatId(chatId.toString());
                        response.setText(msg);
                        response.setReplyMarkup(jobSeekerHandler.getCategoryInlineKeyboard(Optional.empty()));
                        executeMessage(response);
                        return;
                    }

                    String titleMsg = "💼 **Topilgan vakansiyalar (" + vacancies.size() + "):**";
                    SendMessage titleResponse = new SendMessage();
                    titleResponse.setChatId(chatId.toString());
                    titleResponse.setText(titleMsg);
                    titleResponse.setParseMode("Markdown");
                    executeMessage(titleResponse);

                    for (JobVacancy vacancy : vacancies) {
                        String vacancyText = String.format(
                                "📌 **%s**\n📂 Kategoriya: %s\n💼 Turi: %s\n💰 Maosh: %s",
                                vacancy.getTitle(),
                                vacancy.getCategory() != null ? vacancy.getCategory() : "Ko'rsatilmagan",
                                vacancy.getType() != null ? vacancy.getType() : "Ko'rsatilmagan",
                                vacancy.getSalary() != null ? vacancy.getSalary() : "Kelishilgan"
                        );

                        InlineKeyboardMarkup applyMarkup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> applyRows = new ArrayList<>();
                        List<InlineKeyboardButton> applyRow = new ArrayList<>();
                        applyRow.add(InlineKeyboardButton.builder()
                                .text("📝 Ariza topshirish")
                                .callbackData("apply_" + vacancy.getId())
                                .build());
                        applyRows.add(applyRow);
                        applyMarkup.setKeyboard(applyRows);

                        SendMessage vacancyResponse = new SendMessage();
                        vacancyResponse.setChatId(chatId.toString());
                        vacancyResponse.setText(vacancyText);
                        vacancyResponse.setParseMode("Markdown");
                        vacancyResponse.setReplyMarkup(applyMarkup);
                        executeMessage(vacancyResponse);
                    }

                    InlineKeyboardMarkup backMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> backRows = new ArrayList<>();
                    List<InlineKeyboardButton> backRow = new ArrayList<>();
                    backRow.add(InlineKeyboardButton.builder()
                            .text("⬅️ Orqaga")
                            .callbackData("back_to_categories")
                            .build());
                    backRows.add(backRow);
                    backMarkup.setKeyboard(backRows);

                    SendMessage backResponse = new SendMessage();
                    backResponse.setChatId(chatId.toString());
                    backResponse.setText("📂 **Boshqa kategoriyani tanlang:**");
                    backResponse.setParseMode("Markdown");
                    backResponse.setReplyMarkup(backMarkup);
                    executeMessage(backResponse);
                    return;
                }

                // ORQAGA (kategoriyalarga)
                if (callbackData.equals("back_to_categories")) {
                    SendMessage response = new SendMessage();
                    response.setChatId(chatId.toString());
                    response.setText("📂 **Kategoriyani tanlang:**");
                    response.setParseMode("Markdown");
                    response.setReplyMarkup(jobSeekerHandler.getCategoryInlineKeyboard(Optional.empty()));
                    executeMessage(response);
                    return;
                }

                // ARIZA TOPSHIRISH
                if (callbackData.startsWith("apply_")) {
                    Long vacancyId = Long.parseLong(callbackData.replace("apply_", ""));
                    Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
                    if (profileOpt.isEmpty()) {
                        SendMessage response = new SendMessage();
                        response.setChatId(chatId.toString());
                        response.setText("❌ Iltimos, avval ro'yxatdan o'ting!");
                        executeMessage(response);
                        return;
                    }

                    JobVacancy vacancy = jobStore.getAllVacancies().stream()
                            .filter(v -> v.getId().equals(vacancyId))
                            .findFirst().orElse(null);
                    if (vacancy == null) {
                        SendMessage response = new SendMessage();
                        response.setChatId(chatId.toString());
                        response.setText("❌ Vakansiya topilmadi!");
                        executeMessage(response);
                        return;
                    }

                    JobSeekerProfile profile = profileOpt.get();
                    JobApplication application = new JobApplication();
                    application.setJobId(vacancy.getId());
                    application.setJobSeekerId(profile.getId());
                    application.setCoverLetter("Ariza topshirildi");
                    application.setStatus(JobApplication.ApplicationStatus.PENDING);
                    jobApplicationRepository.save(application);

                    Long employerChatId = vacancy.getEmployerChatId();
                    if (employerChatId != null) {
                        String notifyText = String.format(
                                "📩 **Vakansiyangizga yangi ariza keldi!**\n\n" +
                                        "📌 **Vakansiya:** %s\n" +
                                        "👤 **Nomzod:** %s\n" +
                                        "🪪 **Pasport:** %s\n" +
                                        "📞 **Tel:** %s\n" +
                                        "📝 **Tajriba:** %s",
                                vacancy.getTitle(),
                                profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                                profile.getPassportNumber() != null ? profile.getPassportNumber() : "Kiritilmagan",
                                profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                                profile.getExperience() != null ? profile.getExperience() : "Kiritilmagan"
                        );
                        SendMessage notifyMsg = new SendMessage(employerChatId.toString(), notifyText);
                        notifyMsg.setParseMode("Markdown");
                        executeMessage(notifyMsg);
                    }

                    SendMessage response = new SendMessage();
                    response.setChatId(chatId.toString());
                    response.setText("✅ Ariza muvaffaqiyatli topshirildi!");
                    executeMessage(response);
                    return;
                }

            } catch (Exception e) {
                log.error("❌ Xatolik: {}", e.getMessage());
            }
            return;
        }

        // ============================================
        // 2. TEKST XABARLAR
        // ============================================
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";

        log.info("📩 Xabar: chatId={}, text={}", chatId, text);

        // ============================================
        // /start yoki "🔄 Rolni o'zgartirish"
        // ============================================
        if (text.equals("/start") || text.equals("🔄 Rolni o'zgartirish") || text.equals("Asosiy menyu")) {
            userRoles.put(chatId, UserRole.NONE);
            // Til allaqachon tanlanganmi?
            Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
            if (profileOpt.isPresent()) {
                // Profil mavjud - to'g'ridan-to'g'ri rol tanlash
                sendRoleSelectionMenu(chatId, "Hush kelibsiz! Rolingizni tanlang:", profileOpt);
            } else {
                // Profil mavjud emas - til tanlash
                sendLanguageSelectionMenu(chatId);
            }
            return;
        }

        // TIL TANLASH (inline keyboard orqali)
        if (text.equals("/language") || text.equals("🌐 Til") || text.equals("🌐 Язык") || text.equals("🌐 Language")) {
            sendLanguageSelectionMenu(chatId);
            return;
        }

        // ROL TANLASH
        if (text.contains("Employer (Ish beruvchi)") || text.contains("Ish beruvchi (Employer)") ||
                text.contains("Работодатель") || text.equals("Employer")) {
            userRoles.put(chatId, UserRole.EMPLOYER);
        } else if (text.contains("JobSeeker (Ish izlovchi)") || text.contains("Ish izlovchi (JobSeeker)") ||
                text.contains("Соискатель") || text.equals("Job Seeker")) {
            userRoles.put(chatId, UserRole.JOB_SEEKER);
        }

        UserRole role = userRoles.getOrDefault(chatId, UserRole.NONE);

        if (role == UserRole.EMPLOYER) {
            SendMessage response = employerHandler.handleEmployer(message);
            if (response != null) executeMessage(response);
        } else if (role == UserRole.JOB_SEEKER) {
            SendMessage response = jobSeekerHandler.handleJobSeeker(message);
            if (response != null) executeMessage(response);
        } else {
            // Rol tanlanmagan bo'lsa, til tanlash
            sendLanguageSelectionMenu(chatId);
        }
    }

    // ============================================
    // ROL TANLASH KEYBOARD
    // ============================================
    private ReplyKeyboardMarkup getRoleSelectionKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();

        if (profileOpt.isPresent() && "ru".equals(profileOpt.get().getLanguage())) {
            row.add("Работодатель");
            row.add("Соискатель");
        } else if (profileOpt.isPresent() && "en".equals(profileOpt.get().getLanguage())) {
            row.add("Employer");
            row.add("Job Seeker");
        } else {
            row.add("Employer (Ish beruvchi)");
            row.add("JobSeeker (Ish izlovchi)");
        }

        markup.setKeyboard(List.of(row));
        return markup;
    }

    private void sendRoleSelectionMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setReplyMarkup(getRoleSelectionKeyboard(profileOpt));
        executeMessage(message);
    }

    // ============================================
    // TIL TANLASH MENYUSI (INLINE KEYBOARD)
    // ============================================
    private void sendLanguageSelectionMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🌐 **Xush kelibsiz! Iltimos, tilni tanlang:**\n\n" +
                "🌐 **Добро пожаловать! Пожалуйста, выберите язык:**\n\n" +
                "🌐 **Welcome! Please choose your language:**");
        message.setParseMode("Markdown");
        message.setReplyMarkup(getLanguageInlineKeyboard());
        executeMessage(message);
    }

    private InlineKeyboardMarkup getLanguageInlineKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("🇺🇿 O'zbek").callbackData("lang_uz").build());
        row1.add(InlineKeyboardButton.builder().text("🇷🇺 Русский").callbackData("lang_ru").build());
        row1.add(InlineKeyboardButton.builder().text("🇬🇧 English").callbackData("lang_en").build());

        rows.add(row1);
        markup.setKeyboard(rows);
        return markup;
    }

    // ============================================
    // KATEGORIYA NOMI
    // ============================================
    private String getCategoryName(String key) {
        return switch (key) {
            case "it" -> "💻 IT & Dasturlash";
            case "design" -> "🎨 Dizayn";
            case "construction" -> "🏗️ Qurilish";
            case "driver" -> "🚗 Haydovchi / Kuryer";
            case "education" -> "📚 Ta'lim / Repetitor";
            case "trade" -> "🛒 Savdo / Sotuvchi";
            case "cleaner" -> "🧹 Farrosh / Tozalash";
            case "cook" -> "👨‍🍳 Pazanda / Oshpaz";
            case "security" -> "🔒 Qorovul / Xavfsizlik";
            case "courier" -> "📦 Kuryer / Yetkazib berish";
            case "all" -> "Barcha vakansiyalar";
            default -> key;
        };
    }

    // ============================================
    // XABAR YUBORISH
    // ============================================
    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("❌ Xatolik: {}", e.getMessage());
        }
    }
}