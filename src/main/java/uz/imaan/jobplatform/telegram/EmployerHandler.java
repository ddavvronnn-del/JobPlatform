package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.job.JobVacancy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmployerHandler {

    public enum EmployerState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PASSPORT,
        WAITING_FOR_JOB_TYPE,
        WAITING_FOR_PHONE,
        MAIN_MENU,
        WAITING_FOR_JOB_TITLE,
        WAITING_FOR_CATEGORY,
        WAITING_FOR_SALARY,
        MY_VACANCIES,
        SETTINGS_MENU
    }

    private final JobStore jobStore;

    private final Map<Long, EmployerState> states = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> data = new ConcurrentHashMap<>();

    @Autowired
    public EmployerHandler(JobStore jobStore) {
        this.jobStore = jobStore;
    }

    public SendMessage handleEmployer(Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";
        data.putIfAbsent(chatId, new HashMap<>());

        EmployerState state = states.getOrDefault(chatId, EmployerState.NONE);

        // 1. Navigation / Back commands
        if (text.equals("⬅️ Orqaga") || text.equals("❌ Bekor qilish") || text.equals("Asosiy menyu")) {
            states.put(chatId, EmployerState.MAIN_MENU);
            return createMessage(chatId, "🏢 **Ish beruvchi menyusi**\n\nKerakli bo'limni tanlang:", getMainMenuKeyboard());
        }

        // 2. Rolga birinchi kirish / Ro'yxatdan o'tish
        if (text.equals("Employer (Ish beruvchi)") || text.equals("Ish beruvchi (Employer)")) {
            states.put(chatId, EmployerState.WAITING_FOR_NAME);
            return createMessage(chatId, "👤 **Ish beruvchi sifatida ro'yxatdan o'tish:**\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`", getCancelKeyboard());
        }

        // 3. Ro'yxatdan o me'yorida o'tish ketma-ketligi
        if (state == EmployerState.WAITING_FOR_NAME && message.hasText()) {
            data.get(chatId).put("fullName", text);
            states.put(chatId, EmployerState.WAITING_FOR_PASSPORT);
            return createMessage(chatId, "📄 **Pasport seriya va raqamingizni kiriting:**\n\n💡 *Misol:* `AD1234567`", getCancelKeyboard());
        }

        if (state == EmployerState.WAITING_FOR_PASSPORT && message.hasText()) {
            data.get(chatId).put("passport", text);
            states.put(chatId, EmployerState.WAITING_FOR_JOB_TYPE);
            return createMessage(chatId, "💼 **Kompaniya / Ish turini tanlang:**", getJobTypeKeyboard());
        }

        if (state == EmployerState.WAITING_FOR_JOB_TYPE && message.hasText()) {
            data.get(chatId).put("jobType", text);
            states.put(chatId, EmployerState.WAITING_FOR_PHONE);
            return createMessage(chatId, "📱 **Telefon raqamingizni yuboring:**", getPhoneKeyboard());
        }

        if (state == EmployerState.WAITING_FOR_PHONE) {
            String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
            if (!phone.isEmpty()) {
                data.get(chatId).put("phone", phone);
                states.put(chatId, EmployerState.MAIN_MENU);
                return createMessage(chatId, "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**\n\nIsh beruvchi menyusi:", getMainMenuKeyboard());
            }
        }

        // 4. Main Menu commands
        if (state == EmployerState.MAIN_MENU) {
            switch (text) {
                case "➕ Yangi e'lon yaratish":
                    states.put(chatId, EmployerState.WAITING_FOR_JOB_TITLE);
                    return createMessage(chatId, "📝 **Ish sarlavhasini (lavozimni) kiriting:**\n\n💡 *Misol:* `Java Backend Dasturchi`", getCancelKeyboard());

                case "📂 Mening e'lonlarim":
                    states.put(chatId, EmployerState.MY_VACANCIES);
                    return handleShowMyVacancies(chatId);

                case "⚙️ Sozlamalar":
                    states.put(chatId, EmployerState.SETTINGS_MENU);
                    return createMessage(chatId, "⚙️ **Sozlamalar bo'limi:**", getSettingsKeyboard());
            }
        }

        // 5. E'lon yaratish ketma-ketligi
        if (state == EmployerState.WAITING_FOR_JOB_TITLE && message.hasText()) {
            data.get(chatId).put("jobTitle", text);
            states.put(chatId, EmployerState.WAITING_FOR_CATEGORY);
            return createMessage(chatId, "📂 **Vakansiya kategoriyasini tanlang:**", getCategoryKeyboard());
        }

        if (state == EmployerState.WAITING_FOR_CATEGORY && message.hasText()) {
            data.get(chatId).put("category", text);
            states.put(chatId, EmployerState.WAITING_FOR_SALARY);
            return createMessage(chatId, "💰 **Taklif qilinadigan maoshni kiriting:**\n\n💡 *Misol:* `1200$` yoki `5,000,000 so'm`", getCancelKeyboard());
        }

        if (state == EmployerState.WAITING_FOR_SALARY && message.hasText()) {
            data.get(chatId).put("salary", text);

            String title = data.get(chatId).get("jobTitle");
            String category = data.get(chatId).get("category");
            String salary = data.get(chatId).get("salary");
            String type = data.get(chatId).getOrDefault("jobType", "To'liq kun");

            // JobStore ga saqlaymiz
            JobVacancy vacancy = new JobVacancy(chatId, title, category, type, salary);
            jobStore.addVacancy(vacancy);

            states.put(chatId, EmployerState.MAIN_MENU);

            String successText = String.format(
                    "🎉 **E'loningiz muvaffaqiyatli e'lon qilindi!**\n\n" +
                            "📌 **Nomi:** %s\n" +
                            "📂 **Kategoriya:** %s\n" +
                            "⏱ **Turi:** %s\n" +
                            "💰 **Maosh:** %s",
                    title, category, type, salary
            );

            return createMessage(chatId, successText, getMainMenuKeyboard());
        }

        // Statik tugmalarni tekshirish (agarda state o'zgarmay qolgan bo'lsa)
        if (text.equals("➕ Yangi e'lon yaratish")) {
            states.put(chatId, EmployerState.WAITING_FOR_JOB_TITLE);
            return createMessage(chatId, "📝 **Ish sarlavhasini kiriting:**\n\n💡 *Misol:* `Java backend dasturchi`", getCancelKeyboard());
        } else if (text.equals("📂 Mening e'lonlarim")) {
            states.put(chatId, EmployerState.MY_VACANCIES);
            return handleShowMyVacancies(chatId);
        }

        // Agarda employer holatida bo'lmasa, NULL qaytaradi (Telegram.java orqali JobSeeker'ga o'tishi uchun)
        if (state == EmployerState.NONE) {
            return null;
        }

        return createMessage(chatId, "Iltimos, employer menyusidagi tugmalardan birini tanlang.", getMainMenuKeyboard());
    }

    private SendMessage handleShowMyVacancies(Long chatId) {
        List<JobVacancy> myVacancies = jobStore.getVacanciesByEmployer(chatId);

        if (myVacancies.isEmpty()) {
            return createMessage(chatId, "📂 Siz hali hech qanday e'lon joylamagansiz.", getSubBackKeyboard());
        }

        StringBuilder sb = new StringBuilder("📂 **Siz joylagan e'lonlar ro'yxati:**\n\n");
        for (int i = 0; i < myVacancies.size(); i++) {
            JobVacancy v = myVacancies.get(i);
            sb.append(i + 1).append(". 📌 **").append(v.getTitle()).append("**\n")
                    .append("   📂 Kategoriya: ").append(v.getCategory() != null ? v.getCategory() : "Ko'rsatilmagan").append("\n")
                    .append("   💰 Maosh: ").append(v.getSalary() != null ? v.getSalary() : "Kelishilgan").append("\n")
                    .append("───────────────\n");
        }

        return createMessage(chatId, sb.toString(), getSubBackKeyboard());
    }

    // --- KEYBOARDS ---

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("➕ Yangi e'lon yaratish");
        row1.add("📂 Mening e'lonlarim");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⚙️ Sozlamalar");
        row2.add("Asosiy menyu");

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
        row2.add("📢 Marketing");
        row2.add("📊 Moliya va Hisob-kitob");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("❌ Bekor qilish");

        markup.setKeyboard(List.of(row1, row2, row3));
        return markup;
    }

    private ReplyKeyboardMarkup getJobTypeKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🛠 Oddiy ish (Kunlik)");
        row1.add("📋 Rasmiy vakansiya");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("❌ Bekor qilish");

        markup.setKeyboard(List.of(row1, row2));
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

    private ReplyKeyboardMarkup getCancelKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("❌ Bekor qilish");

        markup.setKeyboard(List.of(row));
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

    private ReplyKeyboardMarkup getSettingsKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🌐 Til");
        row1.add("🔔 Bildirishnoma");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅️ Orqaga");

        markup.setKeyboard(List.of(row1, row2));
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