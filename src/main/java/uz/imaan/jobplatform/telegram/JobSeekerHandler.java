package uz.imaan.jobplatform.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobSeekerHandler {
    public SendMessage handleMessage(Long chatId, String text) {
        switch (text) {
            case "JobSeeker (Ish izlovchi)":
            case "Ortga":
                return getJobSeekerMenu(chatId, "Siz Ish izlovchi menyusidasiz. Kerakli bo'limni tanlang:");

            case "Ishlarni ko'rish":
                return getCategoriesMenu(chatId, "Kerakli kategoriyani tanlang:");

            case "Mening arizalarim":
                return createSimpleMessage(chatId, "📋 **Siz topshirgan arizalar:**\n\n1. Java Developer\n2. UI/UX Designer");

            case "IT va Dasturlash":
            case "Dizayn":
            case "Marketing":
            case "Moliya va Hisob-kitob":
                return getJobsByCategory(chatId, text);

            default:
                return createSimpleMessage(chatId, "Iltimos, pastdagi menyu tugmalaridan foydalaning.");
        }
    }

    private SendMessage getJobSeekerMenu(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Ishlarni ko'rish"));
        row1.add(new KeyboardButton("Mening arizalarim"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Asosiy menyu"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    private SendMessage getCategoriesMenu(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("IT va Dasturlash"));
        row1.add(new KeyboardButton("Dizayn"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Marketing"));
        row2.add(new KeyboardButton("Moliya va Hisob-kitob"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Ortga"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    private SendMessage getJobsByCategory(Long chatId, String category) {
        String jobCaption = "📂 **Kategoriya:** " + category + "\n\n" +
                "💼 **Vakansiya:** Middle Java Developer\n" +
                "💰 **Maosh:** $1200 - $1800\n" +
                "📍 **Manzil:** Toshkent shahri";

        SendMessage message = new SendMessage(chatId.toString(), jobCaption);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton applyButton = new InlineKeyboardButton();
        applyButton.setText("📥 Ariza topshirish");
        applyButton.setCallbackData("APPLY_101");

        row.add(applyButton);
        rowsInline.add(row);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    private SendMessage createSimpleMessage(Long chatId, String text) {
        return new SendMessage(chatId.toString(), text);
    }
}
