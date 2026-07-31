package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.util.ArrayList;
import java.util.List;



@Component
public class Telegram extends TelegramLongPollingBot {

    private final JobSeekerHandler jobSeekerHandler;
    private final EmployerHandler employerHandler;

    @Autowired
    public Telegram(JobSeekerHandler jobSeekerHandler, EmployerHandler employerHandler) {
        super("8449248126:AAHly6vbiHKNoCUhG_uc1EU2dfuO4DB6Ycg");
        this.jobSeekerHandler = jobSeekerHandler;
        this.employerHandler = employerHandler;
    }

    @Override
    public String getBotUsername() {
        return "@JobPlatformUzBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            // 1. Asosiy menyular va buyruqlar (faqat matn bo'lganda)
            if (message.hasText()) {
                String text = message.getText();

                if (text.equals("/start") || text.equals("Asosiy menyu")) {
                    sendRoleSelectionMenu(chatId, "Hush kelibsiz! Rolingizni tanlang:");
                    return;
                }

                if (text.contains("Employer") || text.equals("Yangi e'lon yaratish") || text.equals("Mening e'lonlarim")) {
                    SendMessage response = employerHandler.handleEmployer(message);
                    if (response != null) executeMessage(response);
                    return;
                }
            }

            // 2. Qolgan barcha xabarlar (matn, kontakt va h.k.) Ish izlovchiga yo'naltiriladi
            SendMessage response = jobSeekerHandler.handleJobSeeker(message);
            if (response != null) {
                executeMessage(response);
            }
        }
            // 3. Ish Izlovchiga tegishli buyruqlar
            else {
                SendMessage response = jobSeekerHandler.handleJobSeeker(update.getMessage());
                if (response != null) {
                    executeMessage(response);
                }
            }

    }

    private void sendRoleSelectionMenu(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Employer (Ish beruvchi)"));
        row.add(new KeyboardButton("JobSeeker (Ish izlovchi)"));

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}




