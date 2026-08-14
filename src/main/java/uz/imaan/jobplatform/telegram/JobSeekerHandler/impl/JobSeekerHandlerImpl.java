package uz.imaan.jobplatform.telegram.JobSeekerHandler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import uz.imaan.jobplatform.jobseeker.dto.BankCardRequest;
import uz.imaan.jobplatform.jobseeker.entity.BankCard;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobApplicationRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;
import uz.imaan.jobplatform.jobseeker.service.interfaces.WalletService;
import uz.imaan.jobplatform.telegram.Telegram;
import uz.imaan.jobplatform.telegram.JobSeekerHandler.interfaces.JobSeekerHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobSeekerHandlerImpl implements JobSeekerHandler {

    // ============================================
    // ENUM: HOLATLAR
    // ============================================
    public enum JobSeekerState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PASSPORT,
        WAITING_FOR_PHONE,
        WAITING_FOR_JOB_TYPE,
        WAITING_FOR_CATEGORY,
        WAITING_FOR_CUSTOM_CATEGORY,
        WAITING_FOR_EXPERIENCE,
        WAITING_FOR_PROFESSION,
        WAITING_FOR_LANGUAGE,
        MAIN_MENU,
        JOB_SEARCH,
        VIEW_JOB_DETAILS,
        ACTIVE_JOBS,
        PROFILE_MENU,
        APPLICATIONS,
        WALLET_MENU,
        SETTINGS_MENU,
        APPLY_COMMENT,
        WAITING_FOR_EDIT_NAME,
        WAITING_FOR_CARD_NUMBER,
        WAITING_FOR_CARD_HOLDER
    }

    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobStore jobStore;
    private final WalletService walletService;
    private final ApplicationContext applicationContext;

    private final Map<Long, JobSeekerState> states = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> data = new ConcurrentHashMap<>();

    // ============================================
    // TIL TEKSHIRISH METODLARI
    // ============================================
    private boolean isRussian(Optional<JobSeekerProfile> profileOpt) {
        return profileOpt != null && profileOpt.isPresent() && "ru".equals(profileOpt.get().getLanguage());
    }

    private boolean isEnglish(Optional<JobSeekerProfile> profileOpt) {
        return profileOpt != null && profileOpt.isPresent() && "en".equals(profileOpt.get().getLanguage());
    }

    private String getText(Optional<JobSeekerProfile> profileOpt, String ru, String uz, String en) {
        if (isRussian(profileOpt)) return ru;
        if (isEnglish(profileOpt)) return en;
        return uz;
    }

    // ============================================
    // TILNI YANGILASH
    // ============================================
    @Override
    public void updateLanguage(Long chatId, String languageCode) {
        Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
        if (profileOpt.isPresent()) {
            JobSeekerProfile profile = profileOpt.get();
            profile.setLanguage(languageCode);
            jobSeekerProfileRepository.save(profile);
            log.info("✅ Til yangilandi: chatId={}, language={}", chatId, languageCode);
        }
    }

    // ============================================
    // KATEGORIYA INLINE KEYBOARD
    // ============================================
    @Override
    public InlineKeyboardMarkup getCategoryInlineKeyboard(Optional<JobSeekerProfile> profileOpt) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String it, design, construction, driver, education, trade, cleaner, cook, security, courier, all;

        if (isRussian(profileOpt)) {
            it = "💻 IT & Программирование";
            design = "🎨 Дизайн";
            construction = "🏗️ Строительство";
            driver = "🚗 Водитель / Курьер";
            education = "📚 Образование / Репетитор";
            trade = "🛒 Продавец";
            cleaner = "🧹 Уборщик";
            cook = "👨‍🍳 Повар";
            security = "🔒 Охрана";
            courier = "📦 Доставка";
            all = "🌐 Все вакансии";
        } else if (isEnglish(profileOpt)) {
            it = "💻 IT & Programming";
            design = "🎨 Design";
            construction = "🏗️ Construction";
            driver = "🚗 Driver / Courier";
            education = "📚 Education / Tutor";
            trade = "🛒 Sales / Seller";
            cleaner = "🧹 Cleaner";
            cook = "👨‍🍳 Cook / Chef";
            security = "🔒 Security";
            courier = "📦 Delivery / Courier";
            all = "🌐 All vacancies";
        } else {
            it = "💻 IT & Dasturlash";
            design = "🎨 Dizayn";
            construction = "🏗️ Qurilish";
            driver = "🚗 Haydovchi / Kuryer";
            education = "📚 Ta'lim / Repetitor";
            trade = "🛒 Savdo / Sotuvchi";
            cleaner = "🧹 Farrosh / Tozalash";
            cook = "👨‍🍳 Pazanda / Oshpaz";
            security = "🔒 Qorovul / Xavfsizlik";
            courier = "📦 Kuryer / Yetkazib berish";
            all = "🌐 Barcha vakansiyalar";
        }

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text(it).callbackData("category_it").build());
        row1.add(InlineKeyboardButton.builder().text(design).callbackData("category_design").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text(construction).callbackData("category_construction").build());
        row2.add(InlineKeyboardButton.builder().text(driver).callbackData("category_driver").build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text(education).callbackData("category_education").build());
        row3.add(InlineKeyboardButton.builder().text(trade).callbackData("category_trade").build());

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(InlineKeyboardButton.builder().text(cleaner).callbackData("category_cleaner").build());
        row4.add(InlineKeyboardButton.builder().text(cook).callbackData("category_cook").build());

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(InlineKeyboardButton.builder().text(security).callbackData("category_security").build());
        row5.add(InlineKeyboardButton.builder().text(courier).callbackData("category_courier").build());

        List<InlineKeyboardButton> row6 = new ArrayList<>();
        row6.add(InlineKeyboardButton.builder().text(all).callbackData("category_all").build());

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
        rows.add(row5);
        rows.add(row6);
        markup.setKeyboard(rows);
        return markup;
    }

    // ============================================
    // KATEGORIYA BO'YICHA QIDIRISH
    // ============================================
    @Override
    public void handleCategorySearch(Long chatId, String categoryKey, Optional<JobSeekerProfile> profileOpt) {
        try {
            String categoryName = getCategoryName(categoryKey);
            log.info("🔍 Kategoriya bo'yicha qidiruv: chatId={}, category={}", chatId, categoryName);

            List<JobVacancy> vacancies;
            if (categoryKey.equals("all")) {
                vacancies = jobStore.getAllVacancies();
            } else {
                vacancies = jobStore.getAllVacancies().stream()
                        .filter(v -> v.getCategory() != null &&
                                v.getCategory().toLowerCase().contains(categoryName.toLowerCase()))
                        .toList();
            }

            if (vacancies == null || vacancies.isEmpty()) {
                String msg = getText(profileOpt,
                        "❌ Ushbu kategoriya bo'yicha hozircha faol vakansiyalar mavjud emas.",
                        "❌ Ushbu kategoriya bo'yicha hozircha faol vakansiyalar mavjud emas.",
                        "❌ No active vacancies available for this category."
                );
                sendMessage(chatId, msg);
                return;
            }

            StringBuilder result = new StringBuilder();
            String title = getText(profileOpt,
                    "📋 **Topilgan vakansiyalar (" + vacancies.size() + "):**\n\n",
                    "📋 **Topilgan vakansiyalar (" + vacancies.size() + "):**\n\n",
                    "📋 **Found vacancies (" + vacancies.size() + "):**\n\n"
            );
            result.append(title);

            for (int i = 0; i < vacancies.size(); i++) {
                JobVacancy vacancy = vacancies.get(i);
                result.append(i + 1).append(". 📌 **").append(vacancy.getTitle()).append("**\n");
                result.append("   📂 Kategoriya: ").append(vacancy.getCategory()).append("\n");
                result.append("   💰 Maosh: ").append(vacancy.getSalary()).append("\n");
                result.append("───────────────\n");
            }

            InlineKeyboardMarkup backMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> backRows = new ArrayList<>();
            List<InlineKeyboardButton> backRow = new ArrayList<>();
            String backText = isRussian(profileOpt) ? "⬅️ Назад" : (isEnglish(profileOpt) ? "⬅️ Back" : "⬅️ Orqaga");
            backRow.add(InlineKeyboardButton.builder().text(backText).callbackData("back_to_categories").build());
            backRows.add(backRow);
            backMarkup.setKeyboard(backRows);

            SendMessage response = new SendMessage();
            response.setChatId(chatId.toString());
            response.setText(result.toString());
            response.setParseMode("Markdown");
            response.setReplyMarkup(backMarkup);
            executeMessage(response);

        } catch (Exception e) {
            log.error("❌ Xatolik: {}", e.getMessage());
            String msg = getText(profileOpt,
                    "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.",
                    "❌ Xatolik yuz berdi. Qaytadan urinib ko'ring.",
                    "❌ An error occurred. Please try again."
            );
            sendMessage(chatId, msg);
        }
    }

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

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        try {
            Telegram telegramBot = applicationContext.getBean(Telegram.class);
            telegramBot.execute(message);
        } catch (Exception e) {
            log.error("❌ Xatolik: {}", e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            Telegram telegramBot = applicationContext.getBean(Telegram.class);
            telegramBot.execute(message);
        } catch (Exception e) {
            log.error("❌ Xatolik: {}", e.getMessage());
        }
    }

    // ============================================
    // ASOSIY HANDLE METODI
    // ============================================
    @Override
    public SendMessage handleJobSeeker(Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText().trim() : "";
        data.putIfAbsent(chatId, new ConcurrentHashMap<>());

        Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findByUserId(chatId);
        boolean isRegistered = profileOpt.isPresent();

        JobSeekerState state = states.getOrDefault(chatId, JobSeekerState.NONE);

        // NAVIGATION
        if (text.equals("⬅️ Orqaga") || text.equals("❌ Bekor qilish") ||
                text.equals("⬅️ Назад") || text.equals("🏠 Главное меню") || text.equals("Asosiy menyu") ||
                text.equals("⬅️ Back") || text.equals("🏠 Main menu")) {
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, getMainMenuText(profileOpt), getMainMenuKeyboard(profileOpt));
        }

        // KARTA QO'SHISH
        if (state == JobSeekerState.WAITING_FOR_CARD_NUMBER) {
            return handleCardNumber(chatId, text, profileOpt);
        }
        if (state == JobSeekerState.WAITING_FOR_CARD_HOLDER) {
            return handleCardHolder(chatId, text, profileOpt);
        }

        // TIL TANLASH
        if (state == JobSeekerState.WAITING_FOR_LANGUAGE) {
            return handleLanguageSelection(chatId, text, profileOpt);
        }

        // RO'YXATDAN O'TISH
        if (text.equals("JobSeeker (Ish izlovchi)") || text.equals("Ish izlovchi (JobSeeker)") ||
                text.equals("JobSeeker")) {
            if (isRegistered) {
                states.put(chatId, JobSeekerState.MAIN_MENU);
                return createMessage(chatId, getMainMenuText(profileOpt), getMainMenuKeyboard(profileOpt));
            } else {
                states.put(chatId, JobSeekerState.WAITING_FOR_NAME);
                String msg = getText(profileOpt,
                        "👤 **Регистрация в качестве соискателя:**\n\nВведите ваше имя и фамилию.\n💡 *Пример:* `Ali Valiyev`",
                        "👤 **Ish izlovchi sifatida ro'yxatdan o'tish:**\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`",
                        "👤 **Registration as a job seeker:**\n\nPlease enter your first and last name.\n💡 *Example:* `Ali Valiyev`"
                );
                return createMessage(chatId, msg, null);
            }
        }

        // Step 1: Ism
        if (state == JobSeekerState.WAITING_FOR_NAME && message.hasText()) {
            data.get(chatId).put("fullName", text);
            states.put(chatId, JobSeekerState.WAITING_FOR_PASSPORT);
            String msg = getText(profileOpt,
                    "🪪 **Введите серию и номер паспорта:**\n\n💡 *Пример:* `AA1234567`",
                    "🪪 **Pasport seriya va raqamingizni kiriting:**\n\n💡 *Misol:* `AA1234567`",
                    "🪪 **Enter your passport series and number:**\n\n💡 *Example:* `AA1234567`"
            );
            return createMessage(chatId, msg, null);
        }

        // Step 2: Pasport
        if (state == JobSeekerState.WAITING_FOR_PASSPORT && message.hasText()) {
            data.get(chatId).put("passport", text);
            states.put(chatId, JobSeekerState.WAITING_FOR_PHONE);
            String msg = getText(profileOpt,
                    "📱 **Отправьте свой номер телефона:**",
                    "📱 **Telefon raqamingizni yuboring:**",
                    "📱 **Send your phone number:**"
            );
            return createMessage(chatId, msg, getPhoneKeyboard());
        }

        // Step 3: Telefon raqam
        if (state == JobSeekerState.WAITING_FOR_PHONE) {
            String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
            if (phone != null && !phone.isEmpty()) {
                data.get(chatId).put("phone", phone);
                log.info("📱 Telefon qabul qilindi: chatId={}", chatId);

                states.put(chatId, JobSeekerState.WAITING_FOR_JOB_TYPE);
                String msg = getText(profileOpt,
                        "🛠 **Какую работу вы ищете?**\n\nПожалуйста, выберите один из вариантов:",
                        "🛠 **Qanday turdagi ish qidiryapsiz?**\n\nIltimos, quyidagi tugmalardan birini tanlang:",
                        "🛠 **What type of job are you looking for?**\n\nPlease select one of the options:"
                );
                return createMessage(chatId, msg, getJobTypeKeyboard(profileOpt));
            } else {
                String msg = getText(profileOpt,
                        "❌ Номер телефона не отправлен. Пожалуйста, используйте кнопку ниже:",
                        "❌ Telefon raqam yuborilmadi. Iltimos, pastdagi tugma orqali yuboring:",
                        "❌ Phone number not sent. Please use the button below:"
                );
                return createMessage(chatId, msg, getPhoneKeyboard());
            }
        }

        // ISH TURI
        if (state == JobSeekerState.WAITING_FOR_JOB_TYPE && message.hasText()) {
            String jobType = text;
            log.info("📌 ISH TURI TANLANDI: chatId={}", chatId);

            if (jobType.equals("🔧 Oddiy ishchi") || jobType.equals("🔧 Обычный рабочий") || jobType.equals("🔧 Ordinary worker")) {
                JobSeekerProfile profile = new JobSeekerProfile();
                profile.setUserId(chatId);
                profile.setFullName(data.get(chatId).get("fullName"));
                profile.setPassportNumber(data.get(chatId).get("passport"));
                profile.setPhoneNumber(data.get(chatId).get("phone"));
                profile.setPreferredJobType("Oddiy ishchi");
                profile.setProfession("Oddiy ishchi");
                jobSeekerProfileRepository.save(profile);

                states.put(chatId, JobSeekerState.MAIN_MENU);
                data.remove(chatId);
                String msg = getText(profileOpt,
                        "✅ **Вы успешно зарегистрировались!**\n\n🔧 **Тип работы:** Обычный рабочий",
                        "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**\n\n🔧 **Ish turi:** Oddiy ishchi",
                        "✅ **You have successfully registered!**\n\n🔧 **Job type:** Ordinary worker"
                );
                return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
            }

            if (jobType.equals("👨‍💻 Kasbim bo'yicha") || jobType.equals("👨‍💻 По профессии") || jobType.equals("👨‍💻 By profession")) {
                states.put(chatId, JobSeekerState.WAITING_FOR_CATEGORY);
                String msg = getText(profileOpt,
                        "📂 **Выберите категорию, соответствующую вашей профессии:**",
                        "📂 **Kasbingizga mos kategoriyani tanlang:**",
                        "📂 **Select the category matching your profession:**"
                );
                return createMessage(chatId, msg, getRegistrationCategoryKeyboard(profileOpt));
            }

            return createMessage(chatId,
                    getText(profileOpt,
                            "❌ Пожалуйста, выберите один из вариантов:",
                            "❌ Iltimos, quyidagi tugmalardan birini tanlang:",
                            "❌ Please select one of the options:"
                    ),
                    getJobTypeKeyboard(profileOpt));
        }

        // KATEGORIYA TANLASH (RO'YXATDAN O'TISH)
        if (state == JobSeekerState.WAITING_FOR_CATEGORY && message.hasText()) {
            String category = text;
            log.info("📌 KATEGORIYA TANLANDI: chatId={}, category={}", chatId, category);

            data.get(chatId).put("category", category);
            data.get(chatId).put("profession", category);

            states.put(chatId, JobSeekerState.WAITING_FOR_EXPERIENCE);
            String msg = getText(profileOpt,
                    "📝 **Расскажите о своем опыте работы:**\n\nСколько времени вы работаете?\n💡 *Пример:* `3 года Java разработчиком`",
                    "📝 **Ish tajribangiz haqida ma'lumot bering:**\n\nQancha vaqtdan beri ishlayapsiz?\n💡 *Misol:* `3 yil Java dasturchi`",
                    "📝 **Tell us about your work experience:**\n\nHow long have you been working?\n💡 *Example:* `3 years Java developer`"
            );
            return createMessage(chatId, msg, getCancelKeyboard(profileOpt));
        }

        // TAJRIBA
        if (state == JobSeekerState.WAITING_FOR_EXPERIENCE && message.hasText()) {
            data.get(chatId).put("experience", text);
            log.info("✅ Tajriba qabul qilindi: chatId={}", chatId);

            JobSeekerProfile profile = new JobSeekerProfile();
            profile.setUserId(chatId);
            profile.setFullName(data.get(chatId).get("fullName"));
            profile.setPassportNumber(data.get(chatId).get("passport"));
            profile.setPhoneNumber(data.get(chatId).get("phone"));
            profile.setExperience(data.get(chatId).get("experience"));
            profile.setPreferredJobType("Kasbim bo'yicha");
            profile.setProfession(data.get(chatId).get("profession"));
            profile.setCategory(data.get(chatId).get("category"));
            jobSeekerProfileRepository.save(profile);

            states.put(chatId, JobSeekerState.MAIN_MENU);
            data.remove(chatId);

            String msg = getText(profileOpt,
                    String.format(
                            "✅ **Вы успешно зарегистрировались!**\n\n" +
                                    "📂 **Категория:** %s\n" +
                                    "💼 **Ваша профессия:** %s\n" +
                                    "📝 **Ваш опыт:** %s",
                            profile.getCategory(), profile.getProfession(), profile.getExperience()
                    ),
                    String.format(
                            "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**\n\n" +
                                    "📂 **Kategoriya:** %s\n" +
                                    "💼 **Kasbingiz:** %s\n" +
                                    "📝 **Tajribangiz:** %s",
                            profile.getCategory(), profile.getProfession(), profile.getExperience()
                    ),
                    String.format(
                            "✅ **You have successfully registered!**\n\n" +
                                    "📂 **Category:** %s\n" +
                                    "💼 **Your profession:** %s\n" +
                                    "📝 **Your experience:** %s",
                            profile.getCategory(), profile.getProfession(), profile.getExperience()
                    )
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }

        // ISMNI TAHRIRLASH
        if (state == JobSeekerState.WAITING_FOR_EDIT_NAME && message.hasText()) {
            JobSeekerProfile profile = profileOpt.orElseGet(() -> {
                JobSeekerProfile p = new JobSeekerProfile();
                p.setUserId(chatId);
                return p;
            });
            profile.setFullName(text);
            jobSeekerProfileRepository.save(profile);

            states.put(chatId, JobSeekerState.PROFILE_MENU);
            String msg = getText(profileOpt,
                    "✅ Имя и фамилия обновлены!\n\n👤 Новое Ф.И.О: `" + text + "`",
                    "✅ Ism-familiyangiz yangilandi!\n\n👤 Yangi F.I.O: `" + text + "`",
                    "✅ Name updated!\n\n👤 New name: `" + text + "`"
            );
            return createMessage(chatId, msg, getProfileKeyboard(profileOpt));
        }

        // COVER LETTER
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
                                    "📝 **Tajriba:** %s\n" +
                                    "🛠 **Ish turi:** %s\n" +
                                    "✍️ **Izoh:** %s",
                            selectedJob.getTitle(),
                            profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                            profile.getPassportNumber() != null ? profile.getPassportNumber() : "Kiritilmagan",
                            profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                            profile.getExperience() != null ? profile.getExperience() : "Kiritilmagan",
                            profile.getPreferredJobType() != null ? profile.getPreferredJobType() : "Kiritilmagan",
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
            String msg = getText(profileOpt,
                    "✅ **Заявка отправлена!**",
                    "✅ **Ariza yuborildi!**",
                    "✅ **Application sent!**"
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }

        // ASOSIY MENYU
        switch (text) {
            case "🔍 Ish qidirish":
            case "🔍 Поиск работы":
            case "🔍 Job search":
                states.put(chatId, JobSeekerState.JOB_SEARCH);
                String searchMsg = getText(profileOpt,
                        "📂 **Выберите категорию для поиска:**",
                        "📂 **Kategoriyani tanlang:**",
                        "📂 **Select a category to search:**"
                );
                SendMessage searchMessage = new SendMessage();
                searchMessage.setChatId(chatId.toString());
                searchMessage.setText(searchMsg);
                searchMessage.setParseMode("Markdown");
                searchMessage.setReplyMarkup(getCategoryInlineKeyboard(profileOpt));
                return searchMessage;

            case "⚡ Faol ishlarim":
            case "⚡ Мои активные работы":
            case "⚡ My active jobs":
                states.put(chatId, JobSeekerState.ACTIVE_JOBS);
                String activeMsg = getText(profileOpt,
                        "⚡ **Раздел моих активных работ:**",
                        "⚡ **Faol ishlarim bo'limi:**",
                        "⚡ **My active jobs section:**"
                );
                return createMessage(chatId, activeMsg, getActiveJobsKeyboard(profileOpt));

            case "👤 Profilim":
            case "👤 Мой профиль":
            case "👤 My profile":
                states.put(chatId, JobSeekerState.PROFILE_MENU);
                return showProfile(chatId, profileOpt);

            case "📂 Arizalar":
            case "📂 Мои заявки":
            case "📂 My applications":
                states.put(chatId, JobSeekerState.APPLICATIONS);
                return handleShowApplications(chatId, profileOpt);

            case "💳 Hamyon":
            case "💳 Кошелек":
            case "💳 Wallet":
                states.put(chatId, JobSeekerState.WALLET_MENU);
                return showWallet(chatId, profileOpt);

            case "⚙️ Sozlamalar":
            case "⚙️ Настройки":
            case "⚙️ Settings":
                states.put(chatId, JobSeekerState.SETTINGS_MENU);
                return handleSettingsMenu(chatId, text, profileOpt);
        }

        if (state == JobSeekerState.JOB_SEARCH) {
            return null;
        }

        // VAKANSIYA TANLANGANDA
        if (state == JobSeekerState.VIEW_JOB_DETAILS) {
            if (text.startsWith("📌 [")) {
                try {
                    int index = Integer.parseInt(text.substring(text.indexOf("[") + 1, text.indexOf("]")));
                    List<JobVacancy> allVacancies = jobStore.getAllVacancies();
                    if (index >= 0 && index < allVacancies.size()) {
                        JobVacancy selectedVacancy = allVacancies.get(index);
                        data.get(chatId).put("selectedJobIndex", String.valueOf(index));
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
                        return createMessage(chatId, detail, getJobActionKeyboard(profileOpt));
                    }
                } catch (Exception e) {
                    String msg = getText(profileOpt,
                            "Ошибка при загрузке информации о вакансии.",
                            "Vakansiya ma'lumotlarini yuklashda xatolik.",
                            "Error loading vacancy information."
                    );
                    return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
                }
            }

            if (text.equals("📝 Ariza berish") || text.equals("📝 Подать заявку") || text.equals("📝 Submit application")) {
                states.put(chatId, JobSeekerState.APPLY_COMMENT);
                String msg = getText(profileOpt,
                        "✍️ **Напишите сопроводительное письмо:**\n\nКратко расскажите о себе и своем опыте:",
                        "✍️ **Cover letter (Izoh) yozing:**\n\nO'zingiz va tajribangiz haqida qisqacha ma'lumot qoldiring:",
                        "✍️ **Write a cover letter:**\n\nBriefly tell about yourself and your experience:"
                );
                return createMessage(chatId, msg, getCancelKeyboard(profileOpt));
            }
        }

        // PROFIL MENYUSI
        if (state == JobSeekerState.PROFILE_MENU) {
            return handleProfileMenu(chatId, text, profileOpt);
        }

        // HAMYON MENYUSI
        if (state == JobSeekerState.WALLET_MENU) {
            return handleWalletMenu(chatId, text, profileOpt);
        }

        // FAOL ISHLAR
        if (state == JobSeekerState.ACTIVE_JOBS) {
            if (text.contains("Joriy ishlar") || text.contains("Текущие работы") || text.contains("Current jobs") ||
                    text.contains("Topshiriqlar") || text.contains("Задания") || text.contains("Tasks") ||
                    text.contains("Vazifalar") || text.contains("Задачи") || text.contains("Assignments")) {
                String msg = getText(profileOpt,
                        "📋 На данный момент у вас нет активных работ.",
                        "📋 Hozircha faol ishlaringiz mavjud emas.",
                        "📋 You have no active jobs at the moment."
                );
                return createMessage(chatId, msg, getActiveJobsKeyboard(profileOpt));
            }
        }

        // SOZLAMALAR
        if (state == JobSeekerState.SETTINGS_MENU) {
            return handleSettingsMenu(chatId, text, profileOpt);
        }

        String defaultMsg = getText(profileOpt,
                "Пожалуйста, выберите один из вариантов.",
                "Iltimos, tugmalardan birini tanlang.",
                "Please select one of the options."
        );
        return createMessage(chatId, defaultMsg, getMainMenuKeyboard(profileOpt));
    }

    // ============================================
    // KEYBOARD METODLARI
    // ============================================

    @Override
    public ReplyKeyboardMarkup getMainMenuKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row1.add("🔍 Поиск работы");
            row1.add("⚡ Мои активные работы");
            row2.add("👤 Мой профиль");
            row2.add("📂 Мои заявки");
            row3.add("💳 Кошелек");
            row3.add("⚙️ Настройки");
            row4.add("🏠 Главное меню");
        } else if (isEnglish(profileOpt)) {
            row1.add("🔍 Job search");
            row1.add("⚡ My active jobs");
            row2.add("👤 My profile");
            row2.add("📂 My applications");
            row3.add("💳 Wallet");
            row3.add("⚙️ Settings");
            row4.add("🏠 Main menu");
        } else {
            row1.add("🔍 Ish qidirish");
            row1.add("⚡ Faol ishlarim");
            row2.add("👤 Profilim");
            row2.add("📂 Arizalar");
            row3.add("💳 Hamyon");
            row3.add("⚙️ Sozlamalar");
            row4.add("Asosiy menyu");
        }

        markup.setKeyboard(List.of(row1, row2, row3, row4));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getJobTypeKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row1.add("👨‍💻 По профессии");
            row1.add("🔧 Обычный рабочий");
            row2.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            row1.add("👨‍💻 By profession");
            row1.add("🔧 Ordinary worker");
            row2.add("⬅️ Back");
        } else {
            row1.add("👨‍💻 Kasbim bo'yicha");
            row1.add("🔧 Oddiy ishchi");
            row2.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getRegistrationCategoryKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        KeyboardRow row5 = new KeyboardRow();

        String it, design, construction, driver, education, trade, cleaner, cook, security, courier, back;

        if (isRussian(profileOpt)) {
            it = "💻 IT & Программирование";
            design = "🎨 Дизайн";
            construction = "🏗️ Строительство";
            driver = "🚗 Водитель / Курьер";
            education = "📚 Образование / Репетитор";
            trade = "🛒 Продавец";
            cleaner = "🧹 Уборщик";
            cook = "👨‍🍳 Повар";
            security = "🔒 Охрана";
            courier = "📦 Доставка";
            back = "⬅️ Назад";
        } else if (isEnglish(profileOpt)) {
            it = "💻 IT & Programming";
            design = "🎨 Design";
            construction = "🏗️ Construction";
            driver = "🚗 Driver / Courier";
            education = "📚 Education / Tutor";
            trade = "🛒 Sales / Seller";
            cleaner = "🧹 Cleaner";
            cook = "👨‍🍳 Cook / Chef";
            security = "🔒 Security";
            courier = "📦 Delivery / Courier";
            back = "⬅️ Back";
        } else {
            it = "💻 IT & Dasturlash";
            design = "🎨 Dizayn";
            construction = "🏗️ Qurilish";
            driver = "🚗 Haydovchi / Kuryer";
            education = "📚 Ta'lim / Repetitor";
            trade = "🛒 Savdo / Sotuvchi";
            cleaner = "🧹 Farrosh / Tozalash";
            cook = "👨‍🍳 Pazanda / Oshpaz";
            security = "🔒 Qorovul / Xavfsizlik";
            courier = "📦 Kuryer / Yetkazib berish";
            back = "⬅️ Orqaga";
        }

        row1.add(it);
        row1.add(design);
        row2.add(construction);
        row2.add(driver);
        row3.add(education);
        row3.add(trade);
        row4.add(cleaner);
        row4.add(cook);
        row5.add(security);
        row5.add(courier);

        KeyboardRow row6 = new KeyboardRow();
        row6.add(back);

        markup.setKeyboard(List.of(row1, row2, row3, row4, row5, row6));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getSearchCategoryKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return new ReplyKeyboardMarkup();
    }

    @Override
    public ReplyKeyboardMarkup getProfileKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row1.add("📊 Информация");
            row1.add("📁 Портфолио");
            row1.add("⭐ Рейтинг");
            row2.add("🖼 Фото");
            row2.add("💼 Профессия");
            row2.add("✏️ Редактировать");
            row3.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            row1.add("📊 Information");
            row1.add("📁 Portfolio");
            row1.add("⭐ Rating");
            row2.add("🖼 Photo");
            row2.add("💼 Profession");
            row2.add("✏️ Edit");
            row3.add("⬅️ Back");
        } else {
            row1.add("📊 Ma'lumotlar");
            row1.add("📁 Portfolio");
            row1.add("⭐ Reyting");
            row2.add("🖼 Rasm");
            row2.add("💼 Kasb");
            row2.add("✏️ Tahrirlash");
            row3.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    // ============================================
    // HAMYON KEYBOARD (KARTA BOR/YO'Q)
    // ============================================
    @Override
    public ReplyKeyboardMarkup getWalletKeyboard(Optional<JobSeekerProfile> profileOpt) {
        boolean hasCard = false;
        if (profileOpt.isPresent()) {
            List<BankCard> bankCards = profileOpt.get().getBankCards();
            hasCard = bankCards != null && !bankCards.isEmpty();
        }
        return getWalletKeyboard(profileOpt, hasCard);
    }

    @Override
    public ReplyKeyboardMarkup getWalletKeyboard(Optional<JobSeekerProfile> profileOpt, boolean hasCard) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            if (!hasCard) {
                row1.add("💳 Добавить банковскую карту");
            } else {
                row1.add("🗑️ Удалить карту");
            }
            row1.add("💰 Баланс");
            row2.add("💳 Способы оплаты");
            row2.add("📜 История платежей");
            row3.add("💸 Снять деньги");
            row3.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            if (!hasCard) {
                row1.add("💳 Add bank card");
            } else {
                row1.add("🗑️ Delete card");
            }
            row1.add("💰 Balance");
            row2.add("💳 Payment methods");
            row2.add("📜 Payment history");
            row3.add("💸 Withdraw");
            row3.add("⬅️ Back");
        } else {
            if (!hasCard) {
                row1.add("💳 Bank kartasi qo'shish");
            } else {
                row1.add("🗑️ Kartani o'chirish");
            }
            row1.add("💰 Hisob balansi");
            row2.add("💳 To'lov usullari");
            row2.add("📜 To'lov tarixi");
            row3.add("💸 Pul yechish");
            row3.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getSettingsKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row1.add("🌐 Язык");
            row1.add("🔒 Конфиденциальность");
            row2.add("🔔 Уведомления");
            row2.add("❓ Помощь");
            row3.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            row1.add("🌐 Language");
            row1.add("🔒 Privacy");
            row2.add("🔔 Notifications");
            row2.add("❓ Help");
            row3.add("⬅️ Back");
        } else {
            row1.add("🌐 Til");
            row1.add("🔒 Maxfiylik");
            row2.add("🔔 Bildirishnoma");
            row2.add("❓ Yordam");
            row3.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getLanguageKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🇺🇿 O'zbek");
        row1.add("🇷🇺 Русский");
        row1.add("🇬🇧 English");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getActiveJobsKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row1.add("📌 Текущие работы");
            row1.add("📋 Задания");
            row1.add("📝 Задачи");
            row2.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            row1.add("📌 Current jobs");
            row1.add("📋 Tasks");
            row1.add("📝 Assignments");
            row2.add("⬅️ Back");
        } else {
            row1.add("📌 Joriy ishlar");
            row1.add("📋 Topshiriqlar");
            row1.add("📝 Vazifalar");
            row2.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getJobActionKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row1.add("📝 Подать заявку");
            row2.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            row1.add("📝 Submit application");
            row2.add("⬅️ Back");
        } else {
            row1.add("📝 Ariza berish");
            row2.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row1, row2));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getCancelKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        KeyboardRow row = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row.add("❌ Отмена");
        } else if (isEnglish(profileOpt)) {
            row.add("❌ Cancel");
        } else {
            row.add("❌ Bekor qilish");
        }

        markup.setKeyboard(List.of(row));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getSubBackKeyboard(Optional<JobSeekerProfile> profileOpt) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        KeyboardRow row = new KeyboardRow();

        if (isRussian(profileOpt)) {
            row.add("⬅️ Назад");
        } else if (isEnglish(profileOpt)) {
            row.add("⬅️ Back");
        } else {
            row.add("⬅️ Orqaga");
        }

        markup.setKeyboard(List.of(row));
        return markup;
    }

    @Override
    public ReplyKeyboardMarkup getPhoneKeyboard() {
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
    // PROFIL METODLARI
    // ============================================

    @Override
    public SendMessage handleProfileMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        JobSeekerProfile profile = profileOpt.orElse(new JobSeekerProfile());
        double rating = profile.getRating() != null ? profile.getRating() : 0.0;

        if (text.contains("Ma'lumotlar") || text.contains("Информация") || text.contains("Information")) {
            return createMessage(chatId, getProfileInfo(profile, profileOpt), getProfileKeyboard(profileOpt));
        }
        if (text.contains("Portfolio") || text.contains("Портфолио") || text.contains("Portfolio")) {
            String msg = getText(profileOpt,
                    "📁 **Раздел портфолио:**\n\nПортфолио пока не загружено.",
                    "📁 **Portfolio bo'limi:**\n\nHozircha portfolio yuklanmagan.",
                    "📁 **Portfolio section:**\n\nPortfolio not uploaded yet."
            );
            return createMessage(chatId, msg, getSubBackKeyboard(profileOpt));
        }
        if (text.contains("Reyting") || text.contains("Рейтинг") || text.contains("Rating")) {
            String msg = getText(profileOpt,
                    String.format("⭐ **Ваш текущий рейтинг:** %.1f / 5.0", rating),
                    String.format("⭐ **Sizning joriy reytingingiz:** %.1f / 5.0", rating),
                    String.format("⭐ **Your current rating:** %.1f / 5.0", rating)
            );
            return createMessage(chatId, msg, getProfileKeyboard(profileOpt));
        }
        if (text.contains("Rasm") || text.contains("Фото") || text.contains("Photo")) {
            String msg = getText(profileOpt,
                    "🖼 **Фото профиля:**\n\nОтправьте боту фото для обновления аватара:",
                    "🖼 **Profil rasmi:**\n\nProfil rasmingizni yangilash uchun botga rasm yuboring:",
                    "🖼 **Profile photo:**\n\nSend a photo to update your avatar:"
            );
            return createMessage(chatId, msg, getSubBackKeyboard(profileOpt));
        }
        if (text.contains("Kasb") || text.contains("Профессия") || text.contains("Profession")) {
            states.put(chatId, JobSeekerState.WAITING_FOR_PROFESSION);
            String profession = profile.getProfession() != null ? profile.getProfession() : "Не указана";
            String msg = getText(profileOpt,
                    String.format("💼 **Ваша текущая профессия:** %s\n\nВведите новую профессию:", profession),
                    String.format("💼 **Joriy kasbingiz:** %s\n\nKasbingizni o'zgartirish uchun yangi kasb nomini kiriting:", profession),
                    String.format("💼 **Your current profession:** %s\n\nEnter new profession:", profession)
            );
            return createMessage(chatId, msg, getSubBackKeyboard(profileOpt));
        }
        if (text.contains("Tahrirlash") || text.contains("Редактировать") || text.contains("Edit")) {
            states.put(chatId, JobSeekerState.WAITING_FOR_EDIT_NAME);
            String msg = getText(profileOpt,
                    "✏️ **Редактирование профиля:**\n\nВведите новое имя и фамилию:",
                    "✏️ **Profilni tahrirlash:**\n\nYangi ism va familiyangizni kiriting:",
                    "✏️ **Edit profile:**\n\nEnter new name:"
            );
            return createMessage(chatId, msg, getSubBackKeyboard(profileOpt));
        }
        String msg = getText(profileOpt, "👤 **Меню профиля**", "👤 **Profil menyusi**", "👤 **Profile menu**");
        return createMessage(chatId, msg, getProfileKeyboard(profileOpt));
    }


    @Override
    public String getProfileInfo(JobSeekerProfile profile, Optional<JobSeekerProfile> profileOpt) {
        String jobTypeDisplay = "🔧 Обычный рабочий";
        if ("Kasbim bo'yicha".equals(profile.getPreferredJobType())) {
            jobTypeDisplay = "👨‍💻 " + profile.getProfession();
        }

        // ✅ KARTALARNI OLISH (TO'LIQ RAQAM)
        String cardDisplay = "❌ Karta mavjud emas";
        try {
            if (profile.getBankCards() != null) {
                List<BankCard> bankCards = profile.getBankCards();
                if (!bankCards.isEmpty()) {
                    BankCard activeCard = bankCards.stream()
                            .filter(card -> card.getIsActive() != null && card.getIsActive())
                            .findFirst()
                            .orElse(bankCards.get(0));

                    String cardNumber = activeCard.getCardNumber();
                    if (cardNumber != null && !cardNumber.isEmpty()) {
                        // ✅ TO'LIQ KARTA RAQAMI (yulduzcha YO'Q)
                        cardDisplay = cardNumber;
                    }
                    if (bankCards.size() > 1) {
                        cardDisplay += " (+" + (bankCards.size() - 1) + " ta karta)";
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Karta ma'lumotlarini olishda xatolik: {}", e.getMessage());
            cardDisplay = "❌ Karta ma'lumoti olinmadi";
        }

        // ============================================
        // RUS TILI
        // ============================================
        if (isRussian(profileOpt)) {
            return String.format(
                    "👤 **Информация профиля:**\n\n" +
                            "📌 **Ф.И.О:** %s\n" +
                            "🪪 **Паспорт:** %s\n" +
                            "📞 **Телефон:** %s\n" +
                            "⭐ **Рейтинг:** %.1f\n" +
                            "💼 **Профессия:** %s\n" +
                            "📝 **Опыт:** %s\n" +
                            "🛠 **Тип работы:** %s\n" +
                            "💳 **Карта:** %s",
                    profile.getFullName() != null ? profile.getFullName() : "Не указано",
                    profile.getPassportNumber() != null ? profile.getPassportNumber() : "Не указан",
                    profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Не указан",
                    profile.getRating() != null ? profile.getRating() : 0.0,
                    profile.getProfession() != null ? profile.getProfession() : "Не указана",
                    profile.getExperience() != null ? profile.getExperience() : "Не указан",
                    jobTypeDisplay,
                    cardDisplay
            );
        }

        // ============================================
        // INGLIZ TILI
        // ============================================
        if (isEnglish(profileOpt)) {
            return String.format(
                    "👤 **Profile information:**\n\n" +
                            "📌 **Full name:** %s\n" +
                            "🪪 **Passport:** %s\n" +
                            "📞 **Phone:** %s\n" +
                            "⭐ **Rating:** %.1f\n" +
                            "💼 **Profession:** %s\n" +
                            "📝 **Experience:** %s\n" +
                            "🛠 **Job type:** %s\n" +
                            "💳 **Card:** %s",
                    profile.getFullName() != null ? profile.getFullName() : "Not specified",
                    profile.getPassportNumber() != null ? profile.getPassportNumber() : "Not specified",
                    profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Not specified",
                    profile.getRating() != null ? profile.getRating() : 0.0,
                    profile.getProfession() != null ? profile.getProfession() : "Not specified",
                    profile.getExperience() != null ? profile.getExperience() : "Not specified",
                    jobTypeDisplay,
                    cardDisplay
            );
        }

        // ============================================
        // O'ZBEK TILI (DEFAULT)
        // ============================================
        return String.format(
                "👤 **Profil ma'lumotlari:**\n\n" +
                        "📌 **F.I.O:** %s\n" +
                        "🪪 **Pasport:** %s\n" +
                        "📞 **Tel:** %s\n" +
                        "⭐ **Reyting:** %.1f\n" +
                        "💼 **Kasb:** %s\n" +
                        "📝 **Tajriba:** %s\n" +
                        "🛠 **Ish turi:** %s\n" +
                        "💳 **Karta:** %s",
                profile.getFullName() != null ? profile.getFullName() : "Kiritilmagan",
                profile.getPassportNumber() != null ? profile.getPassportNumber() : "Kiritilmagan",
                profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "Kiritilmagan",
                profile.getRating() != null ? profile.getRating() : 0.0,
                profile.getProfession() != null ? profile.getProfession() : "Ko'rsatilmagan",
                profile.getExperience() != null ? profile.getExperience() : "Ko'rsatilmagan",
                jobTypeDisplay,
                cardDisplay
        );
    }

    // ============================================
    // KARTANI FORMATLASH (12 YULDUZCHA + OXIRGI 4 RAQAM)
    // ============================================

    private String formatCardNumberForProfile(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "❌ Karta mavjud emas";
        }
        if (cardNumber.length() < 4) {
            return "❌ Karta raqami noto'g'ri";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    @Override
    public SendMessage showProfile(Long chatId, Optional<JobSeekerProfile> profileOpt) {
        if (profileOpt.isEmpty()) {
            String msg = getText(profileOpt,
                    "❌ Вы еще не зарегистрированы!",
                    "❌ Siz hali ro'yxatdan o'tmagansiz!",
                    "❌ You are not registered yet!"
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }
        JobSeekerProfile profile = profileOpt.get();
        return createMessage(chatId, getProfileInfo(profile, profileOpt), getProfileKeyboard(profileOpt));
    }

    // ============================================
    // HAMYON METODLARI
    // ============================================

    @Override
    public SendMessage handleWalletMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        // ✅ KARTA QO'SHISH
        if (text.contains("Bank kartasi qo'shish") || text.contains("💳 Bank kartasi") ||
                text.contains("Добавить банковскую карту") || text.contains("💳 Банковская карта") ||
                text.contains("Add bank card") || text.contains("💳 Bank card")) {

            // Karta borligini tekshirish
            if (profileOpt.isPresent()) {
                List<BankCard> bankCards = profileOpt.get().getBankCards();
                if (bankCards != null && !bankCards.isEmpty()) {
                    String msg = getText(profileOpt,
                            "⚠️ У вас уже есть карта! Сначала удалите текущую карту.",
                            "⚠️ Sizda allaqachon karta mavjud! Avval kartani o'chiring.",
                            "⚠️ You already have a card! Please delete the current card first."
                    );
                    return createMessage(chatId, msg, getWalletKeyboard(profileOpt, true));
                }
            }

            states.put(chatId, JobSeekerState.WAITING_FOR_CARD_NUMBER);
            String msg = getText(profileOpt,
                    "💳 Введите номер вашей карты (16 цифр):",
                    "💳 Karta raqamingizni kiriting (16 xona):",
                    "💳 Enter your card number (16 digits):"
            );
            return createMessage(chatId, msg, getCancelKeyboard(profileOpt));
        }

        // ✅ KARTANI O'CHIRISH
        if (text.contains("Kartani o'chirish") || text.contains("🗑️ Kartani o'chirish") ||
                text.contains("Удалить карту") || text.contains("🗑️ Удалить карту") ||
                text.contains("Delete card") || text.contains("🗑️ Delete card")) {
            return handleDeleteCard(chatId, profileOpt);
        }

        if (text.contains("Hisob balansi") || text.contains("💰 Hisob balansi") ||
                text.contains("Баланс") || text.contains("💰 Баланс") ||
                text.contains("Balance") || text.contains("💰 Balance")) {
            return showWallet(chatId, profileOpt);
        }
        if (text.contains("Pul yechish") || text.contains("💸 Pul yechish") ||
                text.contains("Снять деньги") || text.contains("💸 Снять деньги") ||
                text.contains("Withdraw") || text.contains("💸 Withdraw")) {
            String msg = getText(profileOpt,
                    "⚠️ Минимальная сумма для снятия: 50,000 сум.",
                    "⚠️ Pul yechish uchun minimal summa: 50,000 so'm.",
                    "⚠️ Minimum withdrawal amount: 50,000 sum."
            );
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt));
        }
        if (text.contains("To'lov usullari") || text.contains("💳 To'lov usullari") ||
                text.contains("Способы оплаты") || text.contains("💳 Способы оплаты") ||
                text.contains("Payment methods") || text.contains("💳 Payment methods")) {
            String msg = getText(profileOpt,
                    "💳 **Доступные способы оплаты:**\n\n- Click\n- Payme\n- Uzum Bank",
                    "💳 **Mavjud to'lov usullari:**\n\n- Click\n- Payme\n- Uzum Bank",
                    "💳 **Available payment methods:**\n\n- Click\n- Payme\n- Uzum Bank"
            );
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt));
        }
        if (text.contains("To'lov tarixi") || text.contains("📜 To'lov tarixi") ||
                text.contains("История платежей") || text.contains("📜 История платежей") ||
                text.contains("Payment history") || text.contains("📜 Payment history")) {
            String msg = getText(profileOpt,
                    "📜 **История платежей:**\n\nНа данный момент операций нет.",
                    "📜 **To'lovlar tarixi:**\n\nHozircha amaliyotlar mavjud emas.",
                    "📜 **Payment history:**\n\nNo transactions yet."
            );
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt));
        }
        return null;
    }

    // ============================================
    // KARTANI O'CHIRISH
    // ============================================
    @Override
    public SendMessage handleDeleteCard(Long chatId, Optional<JobSeekerProfile> profileOpt) {
        try {
            if (profileOpt.isEmpty()) {
                String msg = getText(profileOpt,
                        "❌ Вы еще не зарегистрированы!",
                        "❌ Siz hali ro'yxatdan o'tmagansiz!",
                        "❌ You are not registered yet!"
                );
                return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
            }

            JobSeekerProfile profile = profileOpt.get();
            List<BankCard> bankCards = profile.getBankCards();

            if (bankCards == null || bankCards.isEmpty()) {
                String msg = getText(profileOpt,
                        "❌ У вас нет сохраненных карт.",
                        "❌ Sizda saqlangan karta mavjud emas.",
                        "❌ You have no saved cards."
                );
                return createMessage(chatId, msg, getWalletKeyboard(profileOpt, false));
            }

            // Birinchi kartani o'chirish
            BankCard cardToDelete = bankCards.get(0);
            bankCards.remove(cardToDelete);
            profile.setBankCards(bankCards);
            jobSeekerProfileRepository.save(profile);

            String msg = getText(profileOpt,
                    "✅ Карта успешно удалена!",
                    "✅ Karta muvaffaqiyatli o'chirildi!",
                    "✅ Card successfully deleted!"
            );
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt, false));

        } catch (Exception e) {
            log.error("❌ Kartani o'chirishda xatolik: {}", e.getMessage());
            String msg = getText(profileOpt,
                    "❌ Ошибка при удалении карты: " + e.getMessage(),
                    "❌ Kartani o'chirishda xatolik: " + e.getMessage(),
                    "❌ Error deleting card: " + e.getMessage()
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }
    }




    @Override
    public SendMessage showWallet(Long chatId, Optional<JobSeekerProfile> profileOpt) {
        try {
            Optional<JobSeekerProfile> profile = jobSeekerProfileRepository.findByUserId(chatId);
            if (profile.isEmpty()) {
                String msg = getText(profileOpt,
                        "❌ Вы еще не зарегистрированы!",
                        "❌ Siz hali ro'yxatdan o'tmagansiz!",
                        "❌ You are not registered yet!"
                );
                return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
            }

            JobSeekerProfile p = profile.get();
            String balance = p.getWalletBalance() != null ? p.getWalletBalance().toString() : "0";

            // ✅ KARTA MA'LUMOTLARINI OLISH (TO'LIQ RAQAM + EGASI)
            String cardNumberDisplay = "❌ Karta mavjud emas";
            String cardHolderDisplay = "";
            List<BankCard> bankCards = p.getBankCards();

            if (bankCards != null && !bankCards.isEmpty()) {
                BankCard activeCard = bankCards.stream()
                        .filter(card -> card.getIsActive() != null && card.getIsActive())
                        .findFirst()
                        .orElse(bankCards.get(0));

                String cardNumber = activeCard.getCardNumber();
                String cardHolderName = activeCard.getCardHolderName();

                // ✅ TO'LIQ KARTA RAQAMI
                if (cardNumber != null && !cardNumber.isEmpty()) {
                    cardNumberDisplay = cardNumber;
                }

                // ✅ KARTA EGASI
                if (cardHolderName != null && !cardHolderName.isEmpty()) {
                    cardHolderDisplay = "👤 **Karta egasi:** " + cardHolderName;
                }

                if (bankCards.size() > 1) {
                    cardNumberDisplay += " (+" + (bankCards.size() - 1) + " ta karta)";
                }
            }

            String msg = getText(profileOpt,
                    "💳 **Кошелек и платежи:**\n\n" +
                            "💰 **Баланс:** " + balance + " сум\n" +
                            "💳 **Карта:** " + cardNumberDisplay + "\n" +
                            (cardHolderDisplay.isEmpty() ? "" : cardHolderDisplay + "\n") +
                            "\n📌 Используйте кнопки ниже:",
                    "💳 **Hamyon va To'lovlar:**\n\n" +
                            "💰 **Hisob balansi:** " + balance + " so'm\n" +
                            "💳 **Karta:** " + cardNumberDisplay + "\n" +
                            (cardHolderDisplay.isEmpty() ? "" : cardHolderDisplay + "\n") +
                            "\n📌 Quyidagi tugmalardan foydalaning:",
                    "💳 **Wallet and payments:**\n\n" +
                            "💰 **Balance:** " + balance + " sum\n" +
                            "💳 **Card:** " + cardNumberDisplay + "\n" +
                            (cardHolderDisplay.isEmpty() ? "" : cardHolderDisplay + "\n") +
                            "\n📌 Use the buttons below:"
            );

            boolean hasCard = bankCards != null && !bankCards.isEmpty();
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt, hasCard));

        } catch (Exception e) {
            log.error("❌ Hamyonni ko'rsatishda xatolik: {}", e.getMessage());
            String msg = getText(profileOpt,
                    "❌ Произошла ошибка!",
                    "❌ Xatolik yuz berdi!",
                    "❌ An error occurred!"
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }
    }



    // ============================================
    // ARIZALAR METODLARI
    // ============================================

    @Override
    public SendMessage handleShowApplications(Long chatId, Optional<JobSeekerProfile> profileOpt) {
        if (profileOpt.isEmpty()) {
            String msg = getText(profileOpt,
                    "📂 Чтобы увидеть свои заявки, сначала зарегистрируйтесь.",
                    "📂 Arizalaringizni ko'rish uchun avval ro'yxatdan o'ting.",
                    "📂 To view your applications, please register first."
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }

        Long jobSeekerId = profileOpt.get().getId();
        List<JobApplication> myApps = jobApplicationRepository.findByJobSeekerId(jobSeekerId);

        if (myApps == null || myApps.isEmpty()) {
            String msg = getText(profileOpt,
                    "📂 Вы еще не подавали заявок на вакансии.",
                    "📂 Siz hali hech qanday vakansiyaga ariza topshirmagansiz.",
                    "📂 You have not submitted any applications yet."
            );
            return createMessage(chatId, msg, getSubBackKeyboard(profileOpt));
        }

        String title = getText(profileOpt,
                "📋 **Ваши заявки:**\n\n",
                "📋 **Siz yuborgan arizalar:**\n\n",
                "📋 **Your applications:**\n\n"
        );
        StringBuilder sb = new StringBuilder(title);
        List<JobVacancy> allVacancies = jobStore.getAllVacancies();

        for (JobApplication app : myApps) {
            // Vakansiyani topish
            String jobTitle = allVacancies.stream()
                    .filter(v -> v.getId().equals(app.getJobId()))
                    .map(JobVacancy::getTitle)
                    .findFirst()
                    .orElse("❌ O'chirilgan vakansiya (ID: " + app.getJobId() + ")");

            String statusStr;
            if (isRussian(profileOpt)) {
                statusStr = switch (app.getStatus()) {
                    case PENDING -> "⏳ Ko'rib chiqilmoqda";
                    case ACCEPTED -> "✅ Qabul qilindi";
                    case REJECTED -> "❌ Rad etildi";
                    case CANCELLED -> "🚫 Bekor qilingan";
                };
            } else if (isEnglish(profileOpt)) {
                statusStr = switch (app.getStatus()) {
                    case PENDING -> "⏳ Pending";
                    case ACCEPTED -> "✅ Accepted";
                    case REJECTED -> "❌ Rejected";
                    case CANCELLED -> "🚫 Cancelled";
                };
            } else {
                statusStr = switch (app.getStatus()) {
                    case PENDING -> "⏳ Ko'rib chiqilmoqda";
                    case ACCEPTED -> "✅ Qabul qilindi";
                    case REJECTED -> "❌ Rad etildi";
                    case CANCELLED -> "🚫 Bekor qilingan";
                };
            }

            sb.append("📌 **Vakansiya:** ").append(jobTitle).append("\n")
                    .append("✍️ **Cover letter:** ").append(app.getCoverLetter() != null ? app.getCoverLetter() : "Yo'q").append("\n")
                    .append("📊 **Holat:** ").append(statusStr).append("\n")
                    .append("───────────────\n");
        }
        return createMessage(chatId, sb.toString(), getSubBackKeyboard(profileOpt));
    }

    // ============================================
    // SOZLAMALAR METODLARI
    // ============================================

    @Override
    public SendMessage handleSettingsMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        if (text.equals("🌐 Til") || text.equals("🌐 Язык") || text.equals("🌐 Language") ||
                text.contains("Til") || text.contains("Язык") || text.contains("Language")) {
            states.put(chatId, JobSeekerState.WAITING_FOR_LANGUAGE);
            String msg = getText(profileOpt,
                    "🌐 **Выберите язык / Choose language:**",
                    "🌐 **Tilni tanlang / Choose language:**",
                    "🌐 **Choose language / Tilni tanlang:**"
            );
            return createMessage(chatId, msg, getLanguageKeyboard());
        }

        if (text.equals("🔒 Maxfiylik") || text.equals("🔒 Конфиденциальность") || text.equals("🔒 Privacy") ||
                text.contains("Maxfiylik") || text.contains("Конфиденциальность") || text.contains("Privacy")) {
            String msg = getText(profileOpt,
                    "🔒 **Настройки конфиденциальности:**\n\nВаши данные надежно защищены.",
                    "🔒 **Maxfiylik sozlamalari:**\n\nSizning ma'lumotlaringiz xavfsiz saqlanadi.",
                    "🔒 **Privacy settings:**\n\nYour data is securely protected."
            );
            return createMessage(chatId, msg, getSettingsKeyboard(profileOpt));
        }

        if (text.equals("🔔 Bildirishnoma") || text.equals("🔔 Уведомления") || text.equals("🔔 Notifications") ||
                text.contains("Bildirishnoma") || text.contains("Уведомления") || text.contains("Notifications")) {
            String msg = getText(profileOpt,
                    "🔔 **Уведомления:** Включены ✅",
                    "🔔 **Bildirishnomalar:** Yoniq ✅",
                    "🔔 **Notifications:** Enabled ✅"
            );
            return createMessage(chatId, msg, getSettingsKeyboard(profileOpt));
        }

        if (text.equals("❓ Yordam") || text.equals("❓ Помощь") || text.equals("❓ Help") ||
                text.contains("Yordam") || text.contains("Помощь") || text.contains("Help")) {
            String msg = getText(profileOpt,
                    "❓ **Центр помощи:**\n\nВ случае возникновения проблем свяжитесь с администратором.",
                    "❓ **Yordam markazi:**\n\nMuammo yuzaga kelsa, admin bilan bog'laning.",
                    "❓ **Help center:**\n\nIf you have any problems, contact the administrator."
            );
            return createMessage(chatId, msg, getSettingsKeyboard(profileOpt));
        }

        if (text.equals("⬅️ Orqaga") || text.equals("⬅️ Назад") || text.equals("⬅️ Back")) {
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, getMainMenuText(profileOpt), getMainMenuKeyboard(profileOpt));
        }

        String msg = getText(profileOpt,
                "⚙️ **Раздел настроек:**",
                "⚙️ **Sozlamalar bo'limi:**",
                "⚙️ **Settings section:**"
        );
        return createMessage(chatId, msg, getSettingsKeyboard(profileOpt));
    }

    @Override
    public SendMessage handleLanguageSelection(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        if (text.equals("🇺🇿 O'zbek") || text.contains("O'zbek")) {
            if (profileOpt.isPresent()) {
                profileOpt.get().setLanguage("uz");
                jobSeekerProfileRepository.save(profileOpt.get());
            }
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "✅ Til O'zbek tiliga o'zgartirildi!", getMainMenuKeyboard(profileOpt));
        }

        if (text.equals("🇷🇺 Русский") || text.contains("Русский")) {
            if (profileOpt.isPresent()) {
                profileOpt.get().setLanguage("ru");
                jobSeekerProfileRepository.save(profileOpt.get());
            }
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "✅ Язык изменен на Русский!", getMainMenuKeyboard(profileOpt));
        }

        if (text.equals("🇬🇧 English") || text.contains("English")) {
            if (profileOpt.isPresent()) {
                profileOpt.get().setLanguage("en");
                jobSeekerProfileRepository.save(profileOpt.get());
            }
            states.put(chatId, JobSeekerState.MAIN_MENU);
            return createMessage(chatId, "✅ Language changed to English!", getMainMenuKeyboard(profileOpt));
        }

        if (text.equals("⬅️ Orqaga") || text.equals("⬅️ Назад") || text.equals("⬅️ Back")) {
            states.put(chatId, JobSeekerState.SETTINGS_MENU);
            String msg = getText(profileOpt, "⚙️ Раздел настроек:", "⚙️ Sozlamalar bo'limi:", "⚙️ Settings section:");
            return createMessage(chatId, msg, getSettingsKeyboard(profileOpt));
        }

        states.put(chatId, JobSeekerState.WAITING_FOR_LANGUAGE);
        String msg = getText(profileOpt,
                "❌ Пожалуйста, выберите язык:",
                "❌ Iltimos, tilni tanlang:",
                "❌ Please select a language:"
        );
        return createMessage(chatId, msg, getLanguageKeyboard());
    }

    // ============================================
    // KARTA QO'SHISH METODLARI
    // ============================================

    @Override
    public SendMessage handleCardNumber(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        if (!text.matches("\\d{16}")) {
            return createMessage(chatId, "❌ Karta raqami 16 ta raqamdan iborat bo'lishi kerak!\nQaytadan kiriting:", getCancelKeyboard(profileOpt));
        }
        data.get(chatId).put("cardNumber", text);
        states.put(chatId, JobSeekerState.WAITING_FOR_CARD_HOLDER);
        return createMessage(chatId, "👤 Karta egasi ismini kiriting:\nMasalan: ALI VALIYEV", getCancelKeyboard(profileOpt));
    }

    @Override
    public SendMessage handleCardHolder(Long chatId, String text, Optional<JobSeekerProfile> profileOpt) {
        if (text.length() < 3) {
            return createMessage(chatId, "❌ Karta egasi ismi juda qisqa! Qaytadan kiriting:", getCancelKeyboard(profileOpt));
        }
        Long userId = chatId;
        if (profileOpt.isEmpty()) {
            String msg = getText(profileOpt,
                    "❌ Вы еще не зарегистрированы! Нажмите /start",
                    "❌ Siz hali ro'yxatdan o'tmagansiz! Iltimos, /start bosing.",
                    "❌ You are not registered yet! Please press /start"
            );
            return createMessage(chatId, msg, getMainMenuKeyboard(profileOpt));
        }
        String cardNumber = data.get(chatId).get("cardNumber");

        try {
            BankCardRequest request = new BankCardRequest();
            request.setCardNumber(cardNumber);
            request.setExpireDate("12/99");
            request.setCardHolderName(text.toUpperCase());
            walletService.addBankCard(userId, request);

            states.put(chatId, JobSeekerState.WALLET_MENU);
            data.get(chatId).remove("cardNumber");

            String msg = getText(profileOpt,
                    "✅ Karta muvaffaqiyatli qo'shildi!\n\n" +
                            "💳 Karta: " + cardNumber + "\n" +
                            "👤 Egasi: " + text.toUpperCase(),
                    "✅ Karta muvaffaqiyatli qo'shildi!\n\n" +
                            "💳 Karta: " + cardNumber + "\n" +
                            "👤 Egasi: " + text.toUpperCase(),
                    "✅ Card successfully added!\n\n" +
                            "💳 Card: " + cardNumber + "\n" +
                            "👤 Holder: " + text.toUpperCase()
            );
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt));

        } catch (Exception e) {
            log.error("❌ Karta qo'shishda xatolik: {}", e.getMessage());
            states.put(chatId, JobSeekerState.WALLET_MENU);
            String msg = getText(profileOpt,
                    "❌ Ошибка при добавлении карты: " + e.getMessage(),
                    "❌ Karta qo'shishda xatolik: " + e.getMessage(),
                    "❌ Error adding card: " + e.getMessage()
            );
            return createMessage(chatId, msg, getWalletKeyboard(profileOpt));
        }
    }

    // ============================================
    // MAIN MENU TEXT
    // ============================================
    private String getMainMenuText(Optional<JobSeekerProfile> profileOpt) {
        if (isRussian(profileOpt)) {
            return "🛠 **Меню работника**\n\nВыберите нужный раздел:";
        }
        if (isEnglish(profileOpt)) {
            return "🛠 **Worker menu**\n\nSelect the section you need:";
        }
        return "🛠 **Ishchi menyusi**\n\nKerakli bo'limni tanlang:";
    }

    // ============================================
    // YORDAMCHI METOD
    // ============================================
    private SendMessage createMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        return message;
    }
}