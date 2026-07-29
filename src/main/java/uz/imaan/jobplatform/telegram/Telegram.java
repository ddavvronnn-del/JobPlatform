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
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        // 1. Asosiy menyu va /start buyrug'i
        if (message.hasText()) {
            String text = message.getText();
            if (text.equals("/start") || text.equals("Asosiy menyu") || text.equals("Ortga")) {
                sendRoleSelectionMenu(chatId, "Hush kelibsiz! Rolingizni tanlang:");
                return;
            }
        }

        // 2. Birinchi navbatda Ish Beruvchi (Employer) handleriga yuborib ko'ramiz
        SendMessage employerResponse = employerHandler.handleEmployer(message);
        if (employerResponse != null) {
            executeMessage(employerResponse);
            return; // Agar EmployerHandler javob qaytarsa, shu yerda to'xtaymiz
        }

        // 3. Agar EmployerHandler 'null' qaytarsa, Ish Izlovchi (JobSeeker) handleriga yuboramiz
        SendMessage jobSeekerResponse = jobSeekerHandler.handleJobSeeker(message);
        if (jobSeekerResponse != null) {
            executeMessage(jobSeekerResponse);
            return; // Agar JobSeekerHandler javob qaytarsa, shu yerda to'xtaymiz
        }

        // 4. Agar ikkala handler ham 'null' qaytarsa (noma'lum xabar bo'lsa)
        SendMessage defaultMessage = new SendMessage(chatId.toString(), "Iltimos, pastdagi menyu tugmalaridan foydalaning.");
        executeMessage(defaultMessage);
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
        // Null kelganda exception otmasligi uchun profilaktika
        if (message == null) {
            return;
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}