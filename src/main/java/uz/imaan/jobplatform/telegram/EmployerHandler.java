package uz.imaan.jobplatform.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import uz.imaan.jobplatform.employer.job.JobVacancyRepository;
import uz.imaan.jobplatform.employer.repository.EmployerRepository;
import uz.imaan.jobplatform.employer.state.EmployerState;

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

        // Tilni aniqlash
        Optional<EmployerEntity> profileOpt = employerRepository.findByEmployerChatId(chatId);
        String lang = profileOpt.map(EmployerEntity::getLanguage).orElse("uz");

        // ============================================
        // 1. GLOBAL TUGMALARNI TEKSHIRISH (INTERRUPT)
        // ============================================
        if (isGlobalButton(text)) {
            draftData.get(chatId).clear();

            if (text.equals("⚙️ Sozlamalar") || text.equals("⚙️ Настройки")) {
                states.put(chatId, EmployerState.SETTINGS_MENU);
                return createMessage(chatId, getText(lang, "⚙️ **Sozlamalar bo'limi:**", "⚙️ **Раздел настроек:**"), getSettingsKeyboard(lang));
            }

            if (text.equals("📢 Vakansiya yaratish") || text.equals("📢 Создать вакансию")) {
                states.put(chatId, EmployerState.WAITING_FOR_TITLE);
                return createMessage(chatId, getText(lang, "📝 **Ish sarlavhasini (lavozimni) kiriting:**\n\n*Misol:* Java Backend Dasturchi", "📝 **Введите название вакансии (должность):**\n\n*Пример:* Java Backend Разработчик"), getCancelKeyboard(lang));
            }

            if (text.equals("📋 Mening vakansiyalarim") || text.equals("📋 Мои вакансии")) {
                states.put(chatId, EmployerState.MAIN_MENU);
                return showMyVacancies(chatId, lang);
            }

            if (text.equals("⬅️ Orqaga") || text.equals("⬅️ Назад") || text.equals("❌ Bekor qilish") || text.equals("❌ Отмена") || text.equals("Asosiy menyu") || text.equals("Главное меню")) {
                states.put(chatId, EmployerState.MAIN_MENU);
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
            draftData.get(chatId).put("title", text);
            states.put(chatId, EmployerState.WAITING_FOR_CATEGORY);
            return createMessage(chatId, getText(lang, "📁 **Vakansiya kategoriyasini tanlang:**", "📁 **Выберите категорию вакансии:**"), getCategoryKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_CATEGORY) {
            if (text.equals("➕ Boshqa") || text.equals("➕ Другое")) {
                states.put(chatId, EmployerState.WAITING_FOR_CUSTOM_CATEGORY);
                return createMessage(chatId, getText(lang, "✏️ **O'zingizning kategoriyangizni kiriting:**", "✏️ **Введите название вашей категории:**"), getCancelKeyboard(lang));
            }

            draftData.get(chatId).put("category", text);
            states.put(chatId, EmployerState.WAITING_FOR_TYPE);
            return createMessage(chatId, getText(lang, "⏰ **Ish turini tanlang:**", "⏰ **Выберите тип занятости:**"), getTypeKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_CUSTOM_CATEGORY) {
            draftData.get(chatId).put("category", text);
            states.put(chatId, EmployerState.WAITING_FOR_TYPE);
            return createMessage(chatId, getText(lang, "⏰ **Ish turini tanlang:**", "⏰ **Выберите тип занятости:**"), getTypeKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_TYPE) {
            draftData.get(chatId).put("type", text);
            states.put(chatId, EmployerState.WAITING_FOR_SALARY);
            return createMessage(chatId, getText(lang, "💰 **Maosh miqdorini kiriting:**\n\n*Misol:* 8 000 000 so'm yoki Kelishiladi", "💰 **Укажите размер зарплаты:**\n\n*Пример:* 8 000 000 сум или Договорная"), getCancelKeyboard(lang));
        }

        if (state == EmployerState.WAITING_FOR_SALARY) {
            draftData.get(chatId).put("salary", text);

            JobVacancy vacancy = new JobVacancy();
            vacancy.setEmployerChatId(chatId);
            vacancy.setTitle(draftData.get(chatId).get("title"));
            vacancy.setCategory(draftData.get(chatId).get("category"));
            vacancy.setType(draftData.get(chatId).get("type"));
            vacancy.setSalary(text);

            jobVacancyRepository.save(vacancy);

            states.put(chatId, EmployerState.MAIN_MENU);
            draftData.get(chatId).clear();

            String successMsg = lang.equals("ru") ?
                    String.format("🎉 **Ваша вакансия успешно опубликована!**\n\n📌 **Название:** %s\n📁 **Категория:** %s\n⏰ **Тип:** %s\n💰 **Зарплата:** %s",
                            vacancy.getTitle(), vacancy.getCategory(), vacancy.getType(), vacancy.getSalary()) :
                    String.format("🎉 **E'loningiz muvaffaqiyatli e'lon qilindi!**\n\n📌 **Nomi:** %s\n📁 **Kategoriya:** %s\n⏰ **Turi:** %s\n💰 **Maosh:** %s",
                            vacancy.getTitle(), vacancy.getCategory(), vacancy.getType(), vacancy.getSalary());

            return createMessage(chatId, successMsg, getMainMenuKeyboard(lang));
        }

        return createMessage(chatId, getText(lang, "📢 **Ish beruvchi menyusi:**", "📢 **Меню работодателя:**"), getMainMenuKeyboard(lang));
    }

    private boolean isGlobalButton(String text) {
        List<String> globalButtons = List.of(
                "⚙️ Sozlamalar", "⚙️ Настройки",
                "📢 Vakansiya yaratish", "📢 Создать вакансию",
                "📋 Mening vakansiyalarim", "📋 Мои вакансии",
                "⬅️ Orqaga", "⬅️ Назад",
                "❌ Bekor qilish", "❌ Отмена",
                "Asosiy menyu", "Главное меню"
        );
        return globalButtons.contains(text);
    }

    private void saveLanguage(Long chatId, Optional<EmployerEntity> profileOpt, String lang) {
        EmployerEntity profile = profileOpt.orElseGet(() -> {
            EmployerEntity p = new EmployerEntity();
            p.setEmployerChatId(chatId);
            return p;
        });
        profile.setLanguage(lang);
        employerRepository.save(profile);
    }

    private SendMessage showMyVacancies(Long chatId, String lang) {
        List<JobVacancy> myVacancies = jobVacancyRepository.findByEmployerChatId(chatId);
        if (myVacancies == null || myVacancies.isEmpty()) {
            return createMessage(chatId, getText(lang, "📋 Sizda hali e'lon qilingan vakansiyalar yo'q.", "📋 У вас пока нет опубликованных вакансий."), getMainMenuKeyboard(lang));
        }

        StringBuilder sb = new StringBuilder(getText(lang, "📋 **Mening vakansiyalarim:**\n\n", "📋 **Мои вакансии:**\n\n"));
        for (JobVacancy v : myVacancies) {
            sb.append("📌 ").append(v.getTitle()).append(" | 💰 ").append(v.getSalary()).append("\n");
        }
        return createMessage(chatId, sb.toString(), getMainMenuKeyboard(lang));
    }

    private String getText(String lang, String uzText, String ruText) {
        return "ru".equals(lang) ? ruText : uzText;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        if ("ru".equals(lang)) {
            markup.setKeyboard(List.of(
                    createRow("📢 Создать вакансию", "📋 Мои вакансии"),
                    createRow("📩 Заявки", "⚙️ Настройки")
            ));
        } else {
            markup.setKeyboard(List.of(
                    createRow("📢 Vakansiya yaratish", "📋 Mening vakansiyalarim"),
                    createRow("📩 Kelgan arizalar", "⚙️ Sozlamalar")
            ));
        }
        return markup;
    }

    private ReplyKeyboardMarkup getSettingsKeyboard(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        if ("ru".equals(lang)) {
            markup.setKeyboard(List.of(
                    createRow("🌐 Язык", "🔒 Конфиденциальность"),
                    createRow("⬅️ Назад")
            ));
        } else {
            markup.setKeyboard(List.of(
                    createRow("🌐 Til", "🔒 Maxfiylik"),
                    createRow("⬅️ Orqaga")
            ));
        }
        return markup;
    }

    private ReplyKeyboardMarkup getCategoryKeyboard(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        if ("ru".equals(lang)) {
            markup.setKeyboard(List.of(
                    createRow("💻 IT & Программирование", "🎨 Дизайн & Медиа"),
                    createRow("📈 Маркетинг & SMM", "🚗 Вождение & Доставка"),
                    createRow("🎓 Образование & Тьютор", "🛠 Обслуживание & Сервис"),
                    createRow("🍔 Общепит & HoReCa", "🏗 Строительство & Ремонт"),
                    createRow("📞 Колл-центр & Продажи", "📦 Склад & Логистика"),
                    createRow("💰 Финансы & Учет", "🏥 Медицина & Фармацевтика"),
                    createRow("➕ Другое", "❌ Отмена")
            ));
        } else {
            markup.setKeyboard(List.of(
                    createRow("💻 IT & Dasturlash", "🎨 Dizayn & Mediya"),
                    createRow("📈 Marketing & SMM", "🚗 Haydovchilik & Kuryer"),
                    createRow("🎓 Ta'lim & Repetitorlik", "🛠 Xizmat ko'rsatish"),
                    createRow("🍔 Restoran & Kafeteriya", "🏗 Qurilish & Ta'mirlash"),
                    createRow("📞 Call-markaz & Sotuv", "📦 Omborxona & Logistika"),
                    createRow("💰 Moliya & Buxgalteriya", "🏥 Tibbiyot & Farmatsevtika"),
                    createRow("➕ Boshqa", "❌ Bekor qilish")
            ));
        }
        return markup;
    }

    private ReplyKeyboardMarkup getTypeKeyboard(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        if ("ru".equals(lang)) {
            markup.setKeyboard(List.of(
                    createRow("⏱ Частичная занятость (Part-time)", "⏰ Полная занятость (Full-time)"),
                    createRow("🏠 Удаленная работа (Remote)", "❌ Отмена")
            ));
        } else {
            markup.setKeyboard(List.of(
                    createRow("⏱ Yarim stavka (Part-time)", "⏰ To'liq bandlik (Full-time)"),
                    createRow("🏠 Masofaviy (Remote)", "❌ Bekor qilish")
            ));
        }
        return markup;
    }

    private ReplyKeyboardMarkup getLanguageKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setKeyboard(List.of(
                createRow("🇺🇿 O'zbek tili", "🇷🇺 Русский язык"),
                createRow("⬅️ Orqaga")
        ));
        return markup;
    }

    private ReplyKeyboardMarkup getCancelKeyboard(String lang) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setKeyboard(List.of(createRow("ru".equals(lang) ? "❌ Отмена" : "❌ Bekor qilish")));
        return markup;
    }

    private KeyboardRow createRow(String... titles) {
        KeyboardRow row = new KeyboardRow();
        for (String title : titles) row.add(title);
        return row;
    }

    private SendMessage createMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        if (keyboard != null) message.setReplyMarkup(keyboard);
        return message;
    }
}