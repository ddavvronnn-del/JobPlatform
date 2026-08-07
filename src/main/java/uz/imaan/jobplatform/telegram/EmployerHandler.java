package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.employer.dto.EmployerResponseDTO;
import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.service.interfacee.EmployerService;
import uz.imaan.jobplatform.employer.state.EmployerState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmployerHandler {

    public enum RegistrationState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PASSPORT,
        WAITING_FOR_JOB_TYPE,
        WAITING_FOR_PHONE,
        MAIN_MENU,
        SETTINGS_MENU
    }

    private final JobStore jobStore;
    private final EmployerService employerService;

    private final Map<Long, RegistrationState> regStates = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> data = new ConcurrentHashMap<>();

    @Autowired
    public EmployerHandler(JobStore jobStore, EmployerService employerService) {
        this.jobStore = jobStore;
        this.employerService = employerService;
    }

    public SendMessage handleEmployer(Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";
        data.putIfAbsent(chatId, new HashMap<>());

        RegistrationState regState = regStates.getOrDefault(chatId, RegistrationState.NONE);
        EmployerState jobState = jobStore.getState(chatId);

        // 1. Bekor qilish / Orqaga tugmalari
        if (text.equals("⬅️ Orqaga") || text.equals("❌ Bekor qilish") || text.equals("Asosiy menyu")) {
            regStates.put(chatId, RegistrationState.MAIN_MENU);
            jobStore.clear(chatId);
            return createMessage(chatId, "🏢 **Ish beruvchi menyusi**\n\nKerakli bo'limni tanlang:", getMainMenuKeyboard());
        }

        // 2. Ro'yxatdan o'tish bosqichida bo'lsa
        if (text.equals("Employer (Ish beruvchi)") || text.equals("Ish beruvchi (Employer)")) {
            regStates.put(chatId, RegistrationState.WAITING_FOR_NAME);
            return createMessage(chatId, "👤 **Ish beruvchi sifatida ro'yxatdan o'tish:**\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`", getCancelKeyboard());
        }

        if (regState == RegistrationState.WAITING_FOR_NAME && message.hasText()) {
            data.get(chatId).put("fullName", text);
            regStates.put(chatId, RegistrationState.WAITING_FOR_PASSPORT);
            return createMessage(chatId, "📄 **Pasport seriya va raqamingizni kiriting:**\n\n💡 *Misol:* `AD1234567`", getCancelKeyboard());
        }

        if (regState == RegistrationState.WAITING_FOR_PASSPORT && message.hasText()) {
            data.get(chatId).put("passport", text);
            regStates.put(chatId, RegistrationState.WAITING_FOR_JOB_TYPE);
            return createMessage(chatId, "💼 **Kompaniya / Ish turini tanlang:**", getJobTypeKeyboard());
        }

        if (regState == RegistrationState.WAITING_FOR_JOB_TYPE && message.hasText()) {
            data.get(chatId).put("jobType", text);
            regStates.put(chatId, RegistrationState.WAITING_FOR_PHONE);
            return createMessage(chatId, "📱 **Telefon raqamingizni yuboring:**", getPhoneKeyboard());
        }

        if (regState == RegistrationState.WAITING_FOR_PHONE) {
            String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
            if (!phone.isEmpty()) {
                data.get(chatId).put("phone", phone);
                regStates.put(chatId, RegistrationState.MAIN_MENU);
                return createMessage(chatId, "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**\n\nIsh beruvchi menyusi:", getMainMenuKeyboard());
            }
        }

        // 3. E'lon yaratish bosqichida bo'lsa (EmployerServiceImpl ga yo'naltiriladi)
        if (jobState != EmployerState.NONE || text.equals("➕ Yangi e'lon yaratish")) {
            return employerService.handleEmployer(message);
        }

        // 4. Asosiy menyu buyruqlari
        if (regState == RegistrationState.MAIN_MENU) {
            switch (text) {
                case "📂 Mening e'lonlarim":
                    return handleShowMyVacancies(chatId);

                case "⚙️ Sozlamalar":
                    regStates.put(chatId, RegistrationState.SETTINGS_MENU);
                    return createMessage(chatId, "⚙️ **Sozlamalar bo'limi:**", getSettingsKeyboard());
            }
        }

        if (regState == RegistrationState.NONE) {
            return null;
        }

        return createMessage(chatId, "Iltimos, menyudagi tugmalardan birini tanlang.", getMainMenuKeyboard());
    }

    private SendMessage handleShowMyVacancies(Long chatId) {
        List<EmployerResponseDTO> myVacancies = employerService.getByEmployerChatId(chatId);

        if (myVacancies.isEmpty()) {
            return createMessage(chatId, "📂 Siz hali hech qanday e'lon joylamagansiz.", getSubBackKeyboard());
        }

        StringBuilder sb = new StringBuilder("📂 **Siz joylagan e'lonlar ro'yxati:**\n\n");
        for (int i = 0; i < myVacancies.size(); i++) {
            EmployerResponseDTO v = myVacancies.get(i);
            sb.append(i + 1).append(". 📌 **").append(v.getCategory() != null ? v.getCategory() : "Vakansiya").append("**\n")
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