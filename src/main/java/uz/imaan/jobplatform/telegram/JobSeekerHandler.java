package uz.imaan.jobplatform.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobSeekerHandler {
    public enum JobSeekerState {
        NONE,
        WAITING_FOR_NAME,
        WAITING_FOR_PHONE,
        REGISTERED
    }

    private final Map<Long, JobSeekerState> states = new HashMap<>();
    private final Map<Long, Map<String, String>> data = new HashMap<>();

    public SendMessage handleJobSeeker( Message message) {
        if (message == null) return null;

        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";
        JobSeekerState state = states.getOrDefault(chatId, JobSeekerState.NONE);
        data.putIfAbsent(chatId, new HashMap<>());

        // 1. "JobSeeker (Ish izlovchi)" tugmasi bosilganda
        if (text.equals("JobSeeker (Ish izlovchi)")) {
            if (state == JobSeekerState.REGISTERED) {
                // Ro'yxatdan o'tgan bo'lsa menyuni ko'rsatamiz
                return createMessage(chatId, "Siz Ish izlovchi menyusidasiz. Kerakli bo'limni tanlang:", getJobSeekerMenuKeyboard());
            } else {
                // Ro'yxatdan o'tmagan bo'lsa registratsiyani boshlaymiz
                states.put(chatId, JobSeekerState.WAITING_FOR_NAME);
                return createMessage(chatId, "👤 **Ish izlovchi sifatida ro'yxatdan o'tish:**\n\nIltimos, ism va familiyangizni kiriting.\n💡 *Misol:* `Ali Valiyev`", null);
            }
        }

        // 2. Ro'yxatdan o mezonlari va qadamlar ketma-ketligi (State)
        switch (state) {
            case WAITING_FOR_NAME:
                if (message.hasText()) {
                    data.get(chatId).put("fullName", text);
                    states.put(chatId, JobSeekerState.WAITING_FOR_PHONE);
                    return createMessage(chatId, "📱 **Telefon raqamingizni yuboring:**", getPhoneKeyboard());
                }
                break;

            case WAITING_FOR_PHONE:
                String phone = message.hasContact() ? message.getContact().getPhoneNumber() : text;
                if (!phone.isEmpty()) {
                    data.get(chatId).put("phone", phone);
                    states.put(chatId, JobSeekerState.REGISTERED);
                    return createMessage(chatId, "✅ **Muvaffaqiyatli ro'yxatdan o'tdingiz!**\n\nSiz Ish izlovchi menyusidasiz. Kerakli bo'limni tanlang:", getJobSeekerMenuKeyboard());
                }
                break;

            case REGISTERED:
                // Menyudagi tugmalar bosilganda
                if (text.equals("Ishlarni ko'rish")) {
                    return createMessage(chatId, "🔍 Hozircha bo'sh ish o'rinlari mavjud emas.", getJobSeekerMenuKeyboard());
                } else if (text.equals("Mening arizalarim")) {
                    return createMessage(chatId, "📂 Siz yuborgan arizalar ro'yxati bo'sh.", getJobSeekerMenuKeyboard());
                }
                break;
        }

        return null;
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

    public ReplyKeyboardMarkup getJobSeekerMenuKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Ishlarni ko'rish");
        row1.add("Mening arizalarim");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Asosiy menyu");

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
