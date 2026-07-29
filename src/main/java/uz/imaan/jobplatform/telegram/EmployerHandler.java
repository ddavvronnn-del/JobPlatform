package uz.imaan.jobplatform.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EmployerHandler {

    public SendMessage handleMessage(Long chatId, String text) {
        return null;
    }

    public enum EmployerState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PASSPORT,
        WAITING_FOR_JOB_TYPE,
        WAITING_FOR_PHONE,
        REGISTERED,
        WAITING_FOR_JOB_TITLE,
        WAITING_FOR_SALARY
    }

    private final Map<Long, EmployerState> states = new HashMap<>();
    private final Map<Long, Map<String, String>> data = new HashMap<>();

    public SendMessage handleEmployer(Message message) {
        if (message == null) {
            return null;
        }

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";
        EmployerState state = states.getOrDefault(chatId, EmployerState.NONE);
        data.putIfAbsent(chatId, new HashMap<>());

        // 1. Agar foydalanuvchi "Ish beruvchi (Employer)" tugmasini bossa - Har qanday holatdan ro'yxatdan o'tishga qaytariladi
        if (text.equals("Ish beruvchi (Employer)") || text.equals("Employer (Ish beruvchi)")) {
            states.put(chatId, EmployerState.WAITING_FOR_NAME);
            return createMessage(chatId, "👤 **Ro'yxatdan o'tish:**\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`", null);
        }

        // 2. Birinchi navbatda FOYDANUVCHINING HOLATINI (STATE) tekshiramiz!
        switch (state) {
            case WAITING_FOR_NAME:
                if (message.hasText()) {
                    data.get(chatId).put("fullName", text);
                    states.put(chatId, EmployerState.WAITING_FOR_PASSPORT);
                    return createMessage(chatId, "📄 **Pasport ma'lumotingizni kiriting:**\n\n💡 *Misol:* `AD1234567`", null);
                }
                break;

            case WAITING_FOR_PASSPORT:
                if (message.hasText()) {
                    data.get(chatId).put("passport", text);
                    states.put(chatId, EmployerState.WAITING_FOR_JOB_TYPE);
                    return createMessage(chatId, "💼 **E'lon yoki ish turini tanlang:**", getJobTypeKeyboard());
                }
                break;

            case WAITING_FOR_JOB_TYPE:
                if (message.hasText()) {
                    if (text.contains("Oddiy ish") || text.contains("Rasmiy vakansiya")) {
                        data.get(chatId).put("jobType", text);
                        states.put(chatId, EmployerState.WAITING_FOR_PHONE);
                        return createMessage(chatId, "📱 **Telefon raqamingizni yuboring:**", getPhoneKeyboard());
                    }
                }
                break;

            case WAITING_FOR_PHONE:
                String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
                if (!phone.isEmpty()) {
                    data.get(chatId).put("phone", phone);
                    states.put(chatId, EmployerState.REGISTERED);
                    return createMessage(chatId, "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**", getEmployerMenuKeyboard());
                }
                break;

            case WAITING_FOR_JOB_TITLE:
                if (message.hasText()) {
                    data.get(chatId).put("jobTitle", text);
                    states.put(chatId, EmployerState.WAITING_FOR_SALARY);
                    return createMessage(chatId, "💰 **Ish haqini (maoshni) kiriting:**\n\n💡 *Misol:* `3000000` yoki `150000 kuniga`", null);
                }
                break;

            case WAITING_FOR_SALARY:
                if (message.hasText()) {
                    data.get(chatId).put("salary", text);
                    states.put(chatId, EmployerState.REGISTERED);

                    String title = data.get(chatId).get("jobTitle");
                    String salary = data.get(chatId).get("salary");
                    String type = data.get(chatId).get("jobType");

                    return createMessage(chatId, "🎉 **E'loningiz qabul qilindi!**\n\n" +
                            "• Ish: " + title + "\n• Turi: " + type + "\n• Maosh: " + salary, getEmployerMenuKeyboard());
                }
                break;
        }

        // 3. Agar foydalanuvchi hech qanday holatda bo'lmasa (REGISTERED yoki NONE), Menyu tugmalari ishlaydi
        if (text.equals("Yangi e'lon yaratish")) {
            if (state == EmployerState.REGISTERED) {
                states.put(chatId, EmployerState.WAITING_FOR_JOB_TITLE);
                return createMessage(chatId, "📝 **Ish sarlavhasini kiriting:**\n\n💡 *Misol:* `Java backend dasturchi`", null);
            } else {
                states.put(chatId, EmployerState.WAITING_FOR_NAME);
                return createMessage(chatId, "⚠️ Avval ro'yxatdan o'ting! Ismingizni kiriting:\n💡 *Misol:* `Ali Valiyev`", null);
            }
        } else if (text.equals("Mening e'lonlarim")) {
            return createMessage(chatId, "📂 Siz joylagan e me'lonlar ro'yxati bo'sh.", getEmployerMenuKeyboard());
        }

        // Agar bu handlerga tegishli bo'lmagan xabar bo'lsa null qaytaradi
        return null;
    }

    // --- EMPLOYER TUGMALARI ---

    private ReplyKeyboardMarkup getJobTypeKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("🛠 Oddiy ish (Kunlik)");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("📋 Rasmiy vakansiya");

        rows.add(row1);
        rows.add(row2);
        markup.setKeyboard(rows);
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

    public ReplyKeyboardMarkup getEmployerMenuKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Yangi e'lon yaratish");
        row1.add("Mening e'lonlarim");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Ortga");

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
