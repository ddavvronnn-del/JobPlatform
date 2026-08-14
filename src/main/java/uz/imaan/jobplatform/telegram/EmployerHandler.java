package uz.imaan.jobplatform.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import uz.imaan.jobplatform.employer.job.JobVacancyRepository;
import uz.imaan.jobplatform.employer.repository.EmployerRepository;
import uz.imaan.jobplatform.employer.state.EmployerState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployerHandler {

    private final EmployerRepository employerRepository;
    private final JobVacancyRepository jobVacancyRepository;

    private final Map<Long, EmployerState> states = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> draftData = new ConcurrentHashMap<>();

    public SendMessage handleEmployer(Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText().trim() : "";
        draftData.putIfAbsent(chatId, new ConcurrentHashMap<>());

        Optional<EmployerEntity> profileOpt = employerRepository.findByEmployerChatId(chatId);
        String lang = profileOpt.map(EmployerEntity::getLanguage).orElse("uz");

        // ============================================
        // 1. GLOBAL BUYRUQLAR (INTERRUPT & ASOSIY MENYU)
        // ============================================
        if (isGlobalButton(text)) {
            draftData.get(chatId).clear();
            states.put(chatId, EmployerState.MAIN_MENU);

            if (text.equals("🏠 Asosiy menyu") || text.equals("🏠 Главное меню") || text.equals("Asosiy menyu") || text.equals("/start")) {
                return createMessage(chatId, getText(lang,
                                "🏠 **Boshidan tanlang:**\nIltimos, quyidagi bo'limlardan birini tanlang:",
                                "🏠 **Выберите сначала:**\nПожалуйста, выберите один из разделов ниже:"),
                        getMainMenuKeyboard(lang));
            }

            if (text.equals("👤 Profilim") || text.equals("👤 Профиль")) {
                return showProfile(chatId, profileOpt, lang);
            }

            if (text.equals("⚙️ Sozlamalar") || text.equals("⚙️ Настройки")) {
                states.put(chatId, EmployerState.SETTINGS_MENU);
                return createMessage(chatId, getText(lang, "⚙️ **Sozlamalar bo'limi:**", "⚙️ **Раздел настроек:**"), getSettingsKeyboard(lang));
            }

            if (text.equals("📢 Vakansiya yaratish") || text.equals("📢 Создать вакансию")) {
                states.put(chatId, EmployerState.WAITING_FOR_TITLE);
                return createMessage(chatId, getText(lang, "📝 **Ish sarlavhasini (lavozimni) kiriting:**\n\n*Misol:* Java Backend Dasturchi", "📝 **Введите название вакансии (должность):**\n\n*Пример:* Java Backend Разработчик"), getCancelKeyboard(lang));
            }

            if (text.equals("📋 Mening vakansiyalarim") || text.equals("📋 Мои вакансии")) {
                return showMyVacancies(chatId, lang);
            }

            if (text.equals("⬅️ Orqaga") || text.equals("⬅️ Назад") || text.equals("❌ Bekor qilish") || text.equals("❌ Отмена")) {
                return createMessage(chatId, getText(lang, "📢 **Ish beruvchi menyusi:**", "📢 **Меню работодателя:**"), getMainMenuKeyboard(lang));
            }
        }

        EmployerState state = states.getOrDefault(chatId, EmployerState.MAIN_MENU);

        // ============================================
        // 2. SOZLAMALAR VA TIL SELEKSIYASI
        // ============================================
        if (state == EmployerState.SETTINGS_MENU) {
            if (text.equals("🌐 Til") || text.equals("🌐 Язык")) {
                states.put(chatId, EmployerState.WAITING_FOR_LANGUAGE);
                return createMessage(chatId, "🌐 **Tilni tanlang / Выберите язык:**", getLanguageKeyboard());
            }
            return createMessage(chatId, getText(lang, "⚙️ **Sozlamalar bo'limi:**", "⚙️ **Раздел настроек:**"), getSettingsKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_LANGUAGE) {
            if (text.equals("🇺🇿 O'zbek tili")) {
                saveLanguage(chatId, profileOpt, "uz");
                states.put(chatId, EmployerState.MAIN_MENU);
                return createMessage(chatId, "✅ Til muvaffaqiyatli O'zbek tiliga o'zgartirildi!", getMainMenuKeyboard("uz"));
            }
            if (text.equals("🇷🇺 Русский язык")) {
                saveLanguage(chatId, profileOpt, "ru");
                states.put(chatId, EmployerState.MAIN_MENU);
                return createMessage(chatId, "✅ Язык успешно изменен на Русский!", getMainMenuKeyboard("ru"));
            }
            states.put(chatId, EmployerState.SETTINGS_MENU);
            return createMessage(chatId, getText(lang, "⚙️ Sozlamalar", "⚙️ Настройки"), getSettingsKeyboard(lang));
        }

        // ============================================
        // 3. VAKANSIYA YARATISH BOSQICHLARI
        // ============================================

        if (state == EmployerState.WAITING_FOR_TITLE) {
            if (!isValidTitle(text)) {
                return createMessage(chatId, getText(lang,
                        "⚠️ **Sarlavha noto'g'ri!** 3-100 ta belgi bo'lishi kerak. Qaytadan kiriting:",
                        "⚠️ **Неверный заголовок!** Должно быть 3-100 символов. Введите снова:"), getCancelKeyboard(lang));
            }
            draftData.get(chatId).put("title", text);
            states.put(chatId, EmployerState.WAITING_FOR_CATEGORY);
            return createMessage(chatId, getText(lang, "📁 **Kategoriyani tanlang yoki kiriting:**", "📁 **Выберите или введите категорию:**"), getCategoryKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_CATEGORY) {
            if (!isValidCategory(text)) {
                return createMessage(chatId, getText(lang, "⚠️ **Kategoriya nomini to'g'ri kiriting:**", "⚠️ **Введите правильную категорию:**"), getCategoryKeyboard(lang));
            }

            if (text.equals("➕ Boshqa (Kiritish)") || text.equals("➕ Другое (Ввести)")) {
                states.put(chatId, EmployerState.WAITING_FOR_CUSTOM_CATEGORY);
                return createMessage(chatId, getText(lang, "📝 **Kategoriya nomini qo'lda kiriting:**", "📝 **Введите название категории вручную:**"), getCancelKeyboard(lang));
            }

            draftData.get(chatId).put("category", text);
            states.put(chatId, EmployerState.WAITING_FOR_JOB_TYPE);
            return createMessage(chatId, getText(lang, "⏰ **Ish turini tanlang:**", "⏰ **Выберите тип работы:**"), getTypeKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_CUSTOM_CATEGORY) {
            if (!isValidTitle(text)) {
                return createMessage(chatId, getText(lang, "⚠️ **Kategoriya juda qisqa, qaytadan kiriting:**", "⚠️ **Категория слишком короткая, введите снова:**"), getCancelKeyboard(lang));
            }
            draftData.get(chatId).put("category", text);
            states.put(chatId, EmployerState.WAITING_FOR_JOB_TYPE);
            return createMessage(chatId, getText(lang, "⏰ **Ish turini tanlang:**", "⏰ **Выберите тип работы:**"), getTypeKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_JOB_TYPE) {
            if (!isValidJobType(text)) {
                return createMessage(chatId, getText(lang, "⚠️ **Tugmalardan birini tanlang:**", "⚠️ **Выберите одну из кнопок:**"), getTypeKeyboard(lang));
            }
            draftData.get(chatId).put("type", text);
            states.put(chatId, EmployerState.WAITING_FOR_WORK_HOURS);
            return createMessage(chatId, getText(lang, "⌛️ **Ish soatlarini tanlang (yoki kiriting):**", "⌛️ **Выберите часы работы (или введите):**"), getWorkHoursKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_WORK_HOURS) {
            if (!isValidWorkHours(text)) {
                return createMessage(chatId, getText(lang, "⚠️ **Ish soati noto'g'ri! Iltimos qaytadan kiriting:**", "⚠️ **Неверные часы! Пожалуйста, введите снова:**"), getWorkHoursKeyboard(lang));
            }
            draftData.get(chatId).put("workHours", text);
            states.put(chatId, EmployerState.WAITING_FOR_WORKER_COUNT);
            return createMessage(chatId, getText(lang, "👥 **Nechta xodim kerak? (Raqam kiriting):**", "👥 **Сколько сотрудников нужно? (Введите число):**"), getCancelKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_WORKER_COUNT) {
            if (!isValidWorkerCount(text)) {
                return createMessage(chatId, getText(lang, "⚠️ **Faqat musbat raqam kiriting (masalan, 1 yoki 5):**", "⚠️ **Введите только положительное число (например, 1 или 5):**"), getCancelKeyboard(lang));
            }
            draftData.get(chatId).put("workerCount", text);
            states.put(chatId, EmployerState.WAITING_FOR_SALARY);
            return createMessage(chatId, getText(lang, "💰 **Maoshni kiriting yoki \"Kelishiladi\" tugmasini bosing:**", "💰 **Введите зарплату или нажмите \"Договорная\":**"), getSalaryQuickKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_SALARY) {
            if (!isValidSalary(text)) {
                return createMessage(chatId, getText(lang, "⚠️ **Maosh noto'g'ri. Qaytadan kiriting:**", "⚠️ **Неверная зарплата. Введите снова:**"), getSalaryQuickKeyboard(lang));
            }
            draftData.get(chatId).put("salary", text);
            states.put(chatId, EmployerState.WAITING_FOR_REQUIREMENTS);
            return createMessage(chatId, getText(lang, "📌 **Talablarni kiriting (yoki O'tkazib yuborish):**", "📌 **Введите требования (или Пропустить):**"), getSkipKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_REQUIREMENTS) {
            String reqText = isSkip(text) ? (lang.equals("ru") ? "Не указано" : "Ko'rsatilmadi") : text;
            draftData.get(chatId).put("requirements", reqText);
            states.put(chatId, EmployerState.WAITING_FOR_PHONE);
            return createMessage(chatId, getText(lang, "📞 **Aloqa uchun telefon raqamini kiriting:**\n(Yoki tugma orqali jo'nating)", "📞 **Введите номер телефона для связи:**\n(Или отправьте через кнопку)"), getContactKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_PHONE) {
            String phoneText = message.hasContact() ? message.getContact().getPhoneNumber() : text;

            if (!isValidPhone(phoneText)) {
                return createMessage(chatId, getText(lang,
                                "⚠️ **Telefon raqam noto'g'ri kiritildi!**\n\nMasalan: `+998901234567` formatida kiriting.",
                                "⚠️ **Некорректный номер телефона!**\n\nВведите в формате `+998901234567`."),
                        getContactKeyboard(lang));
            }

            Map<String, String> data = draftData.getOrDefault(chatId, Map.of());

            JobVacancy vacancy = new JobVacancy();
            vacancy.setEmployerChatId(chatId);
            vacancy.setTitle(data.getOrDefault("title", "Ko'rsatilmadi"));
            vacancy.setCategory(data.getOrDefault("category", "Boshqa"));
            vacancy.setType(data.getOrDefault("type", "To'liq bandlik"));
            vacancy.setSalary(data.getOrDefault("salary", "Kelishiladi"));
            vacancy.setWorkHours(data.getOrDefault("workHours", "8 soat"));

            String countStr = data.getOrDefault("workerCount", "1");
            vacancy.setWorkerCount(countStr.matches("\\d+") ? Integer.parseInt(countStr) : 1);

            vacancy.setRequirements(data.getOrDefault("requirements", "Ko'rsatilmadi"));
            vacancy.setPhoneNumber(phoneText);
            vacancy.setIsActive(true);

            jobVacancyRepository.save(vacancy);

            states.put(chatId, EmployerState.MAIN_MENU);
            draftData.get(chatId).clear();

            String successMsg = lang.equals("ru") ?
                    String.format("🎉 **Ваша вакансия успешно опубликована!**\n\n📌 **Название:** %s\n📁 **Категория:** %s\n⏰ **Тип:** %s\n⌛️ **Часы:** %s\n👥 **Количество:** %s\n💰 **Зарплата:** %s\n📞 **Тел:** %s",
                            vacancy.getTitle(), vacancy.getCategory(), vacancy.getType(), vacancy.getWorkHours(), vacancy.getWorkerCount(), vacancy.getSalary(), vacancy.getPhoneNumber()) :
                    String.format("🎉 **E'loningiz muvaffaqiyatli chop etildi!**\n\n📌 **Nomi:** %s\n📁 **Kategoriya:** %s\n⏰ **Turi:** %s\n⌛️ **Vaqti:** %s\n👥 **Xodimlar:** %s ta\n💰 **Maosh:** %s\n📞 **Tel:** %s",
                            vacancy.getTitle(), vacancy.getCategory(), vacancy.getType(), vacancy.getWorkHours(), vacancy.getWorkerCount(), vacancy.getSalary(), vacancy.getPhoneNumber());

            return createMessage(chatId, successMsg, getMainMenuKeyboard(lang));
        }

        return createMessage(chatId, getText(lang, "📢 **Ish beruvchi menyusi:**", "📢 **Меню работодателя:**"), getMainMenuKeyboard(lang));
    }

    // ============================================
    // BILDIRISHNOMA YUBORISH (NOTIFICATION)
    // ============================================
    public SendMessage buildApplicationNotification(
            Long employerChatId,
            Long applicationId,
            Long employeeChatId,
            String vacancyTitle,
            String candidateName,
            String candidatePhone,
            String lang) {

        String text = lang.equals("ru") ?
                String.format("🔔 **Новый отклик на вакансию!**\n\n📌 **Вакансия:** %s\n👤 **Кандидат:** %s\n📞 **Контакты:** %s\n\nВыберите действие ниже:",
                        vacancyTitle, candidateName, candidatePhone) :
                String.format("🔔 **Vakansiyaga yangi ariza tushdi!**\n\n📌 **Vakansiya:** %s\n👤 **Nomzod:** %s\n📞 **Aloqa:** %s\n\nQuyidagilardan birini tanlang:",
                        vacancyTitle, candidateName, candidatePhone);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new java.util.ArrayList<>();

        InlineKeyboardButton acceptBtn = new InlineKeyboardButton();
        acceptBtn.setText(lang.equals("ru") ? "✅ Принять" : "✅ Qabul qilish");
        acceptBtn.setCallbackData("accept_app_" + applicationId + "_" + employeeChatId);

        InlineKeyboardButton rejectBtn = new InlineKeyboardButton();
        rejectBtn.setText(lang.equals("ru") ? "❌ Отклонить" : "❌ Rad etish");
        rejectBtn.setCallbackData("reject_app_" + applicationId + "_" + employeeChatId);

        rows.add(List.of(acceptBtn, rejectBtn));
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage(employerChatId.toString(), text);
        message.setParseMode("Markdown");
        message.setReplyMarkup(markup);

        return message;
    }

    // ============================================
    // PROFIL VA VAKANSIYALARNI KO'RSATISH
    // ============================================
    private SendMessage showProfile(Long chatId, Optional<EmployerEntity> profileOpt, String lang) {
        String rating = "5.0 ⭐";
        String currentLang = profileOpt.map(EmployerEntity::getLanguage).orElse(lang);

        String msg = lang.equals("ru") ?
                String.format("👤 **Ваш профиль:**\n\n🆔 **Telegram ID:** %s\n🌐 **Язык:** %s\n⭐ **Рейтинг:** %s\n📝 **Статус:** Активный работодатель",
                        chatId, currentLang, rating) :
                String.format("👤 **Sizning profilingiz:**\n\n🆔 **Telegram ID:** %s\n🌐 **Til:** %s\n⭐ **Reyting:** %s\n📝 **Holat:** Faol ish beruvchi",
                        chatId, currentLang, rating);

        return createMessage(chatId, msg, getMainMenuKeyboard(lang));
    }

    private SendMessage showMyVacancies(Long chatId, String lang) {
        List<JobVacancy> myJobs = jobVacancyRepository.findAllByEmployerChatId(chatId);
        if (myJobs.isEmpty()) {
            String emptyMsg = lang.equals("ru") ? "У вас пока нет активных вакансий." : "Sizda hozircha faol vakansiyalar yo'q.";
            return createMessage(chatId, emptyMsg, getMainMenuKeyboard(lang));
        }

        StringBuilder sb = new StringBuilder(lang.equals("ru") ? "📋 **Ваши вакансии и зарплаты:**\n\n" : "📋 **Sizning vakansiyalaringiz va maoshlar:**\n\n");
        for (int i = 0; i < myJobs.size(); i++) {
            JobVacancy job = myJobs.get(i);
            sb.append(i + 1).append(". **").append(job.getTitle() != null ? job.getTitle() : "Nomsiz").append("**\n")
                    .append("   💰 Maosh: **").append(job.getSalary() != null ? job.getSalary() : "Kelishiladi").append("**\n")
                    .append("   📁 Kategoriya: ").append(job.getCategory() != null ? job.getCategory() : "Ko'rsatilmagan").append("\n\n");
        }
        return createMessage(chatId, sb.toString(), getMainMenuKeyboard(lang));
    }

    // ============================================
    // VALIDATION & HELPERS
    // ============================================
    private boolean isValidTitle(String text) {
        return text != null && text.length() >= 3 && text.length() <= 100;
    }

    private boolean isValidCategory(String text) {
        return text != null && !text.isBlank() && text.length() <= 50;
    }

    private boolean isValidJobType(String text) {
        return text != null && text.length() > 2;
    }

    private boolean isValidWorkHours(String text) {
        return text != null && !text.isBlank() && text.length() <= 50;
    }

    private boolean isValidWorkerCount(String text) {
        return text != null && text.trim().matches("^[1-9]\\d*$");
    }

    private boolean isValidSalary(String text) {
        return text != null && !text.isBlank() && text.length() <= 100;
    }

    private boolean isValidPhone(String text) {
        if (text == null || text.isBlank()) return false;
        String cleanPhone = text.replaceAll("[^0-9+]", "");
        return cleanPhone.matches("^\\+?[0-9]{9,15}$");
    }

    private boolean isGlobalButton(String text) {
        return text != null && (
                text.equals("/start") ||
                        text.equals("👤 Profilim") || text.equals("👤 Профиль") ||
                        text.equals("⚙️ Sozlamalar") || text.equals("⚙️ Настройки") ||
                        text.equals("📢 Vakansiya yaratish") || text.equals("📢 Создать вакансию") ||
                        text.equals("📋 Mening vakansiyalarim") || text.equals("📋 Мои вакансии") ||
                        text.equals("⬅️ Orqaga") || text.equals("⬅️ Назад") ||
                        text.equals("❌ Bekor qilish") || text.equals("❌ Отмена") ||
                        text.equals("🏠 Asosiy menyu") || text.equals("🏠 Главное меню") || text.equals("Asosiy menyu")
        );
    }

    private boolean isSkip(String text) {
        return text.equals("⏭ O'tkazib yuborish") || text.equals("⏭ Пропустить");
    }

    private void saveLanguage(Long chatId, Optional<EmployerEntity> profileOpt, String lang) {
        if (profileOpt.isPresent()) {
            EmployerEntity profile = profileOpt.get();
            profile.setLanguage(lang);
            employerRepository.save(profile);
        } else {
            EmployerEntity newProfile = EmployerEntity.builder()
                    .employerChatId(chatId)
                    .language(lang)
                    .createdAt(LocalDateTime.now())
                    .build();
            employerRepository.save(newProfile);
        }
    }

    private String getText(String lang, String uz, String ru) {
        return lang.equals("ru") ? ru : uz;
    }

    private SendMessage createMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setParseMode("Markdown");
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }
        return message;
    }

    private ReplyKeyboardMarkup getMainMenuKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of(getText(lang, "📢 Vakansiya yaratish", "📢 Создать вакансию")),
                List.of(getText(lang, "📋 Mening vakansiyalarim", "📋 Мои вакансии")),
                List.of(getText(lang, "👤 Profilim", "👤 Профиль"), getText(lang, "⚙️ Sozlamalar", "⚙️ Настройки")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getSettingsKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of(getText(lang, "🌐 Til", "🌐 Язык")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getLanguageKeyboard() {
        return createReplyKeyboard(List.of(
                List.of("🇺🇿 O'zbek tili", "🇷🇺 Русский язык"),
                List.of("🏠 Asosiy menyu")
        ));
    }

    private ReplyKeyboardMarkup getCancelKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of(getText(lang, "❌ Bekor qilish", "❌ Отмена")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getSkipKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of(getText(lang, "⏭ O'tkazib yuborish", "⏭ Пропустить")),
                List.of(getText(lang, "❌ Bekor qilish", "❌ Отмена")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getContactKeyboard(String lang) {
        KeyboardButton btn = new KeyboardButton(getText(lang, "📞 Raqamni yuborish", "📞 Отправить номер"));
        btn.setRequestContact(true);
        KeyboardButton cancel = new KeyboardButton(getText(lang, "❌ Bekor qilish", "❌ Отмена"));
        KeyboardButton mainMenu = new KeyboardButton(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"));

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row1 = new KeyboardRow(); row1.add(btn);
        KeyboardRow row2 = new KeyboardRow(); row2.add(cancel);
        KeyboardRow row3 = new KeyboardRow(); row3.add(mainMenu);
        keyboardMarkup.setKeyboard(List.of(row1, row2, row3));
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup getCategoryKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of("💻 IT & Dasturlash", "🎨 Dizayn"),
                List.of("🏗️ Qurilish", "🚗 Haydovchi / Kuryer"),
                List.of("📚 Ta'lim / Repetitor", "🛒 Savdo / Sotuvchi"),
                List.of("🧹 Farrosh / Tozalash", "👨‍🍳 Pazanda / Oshpaz"),
                List.of("🔒 Qorovul / Xavfsizlik", "📦 Kuryer / Yetkazib berish"),
                List.of(getText(lang, "➕ Boshqa (Kiritish)", "➕ Другое (Ввести)")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getTypeKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of(getText(lang, "To'liq bandlik (Full-time)", "Полная занятость (Full-time)")),
                List.of(getText(lang, "Qisman bandlik (Part-time)", "Частичная занятость (Part-time)")),
                List.of(getText(lang, "Masofaviy (Remote)", "Удаленная работа (Remote)")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getWorkHoursKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of("09:00 - 18:00", "08:00 - 17:00"),
                List.of(getText(lang, "Moslashuvchan", "Гибкий график")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup getSalaryQuickKeyboard(String lang) {
        return createReplyKeyboard(List.of(
                List.of(getText(lang, "Kelishiladi", "Договорная")),
                List.of(getText(lang, "🏠 Asosiy menyu", "🏠 Главное меню"))
        ));
    }

    private ReplyKeyboardMarkup createReplyKeyboard(List<List<String>> buttons) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = buttons.stream().map(rowStr -> {
            KeyboardRow row = new KeyboardRow();
            rowStr.forEach(row::add);
            return row;
        }).toList();
        markup.setKeyboard(keyboard);
        return markup;
    }
}