package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Telegram extends TelegramLongPollingBot {

    public enum UserRole {
        NONE,
        EMPLOYER,
        JOB_SEEKER
    }

    private final JobSeekerHandler jobSeekerHandler;
    private final EmployerHandler employerHandler;

    private final Map<Long, UserRole> userRoles = new ConcurrentHashMap<>();

    @Autowired
    public Telegram(JobSeekerHandler jobSeekerHandler,
                    EmployerHandler employerHandler,
                    @Value("${bot.token:8449248126:AAFPgTpsBD2o1k_cp8YbG8_wqp9o8KnRCss}") String botToken) {
        super(botToken);
        this.jobSeekerHandler = jobSeekerHandler;
        this.employerHandler = employerHandler;
    }

    @Override
    public String getBotUsername() {
        return "@JobPlatformUzBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        // 1. Inline tugmalar (CallbackQuery) bosilganda qabul qilish
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        // 2. Oddiy xabar bo'lmasa to'xtatish
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";

        // 3. Faqat /start yoki Rolni o'zgartirish bosilganda rol tozalanadi
        if (text.equals("/start") || text.equals("🔄 Rolni o'zgartirish")) {
            userRoles.put(chatId, UserRole.NONE);
            sendRoleSelectionMenu(chatId, "Xush kelibsiz! Rolingizni tanlang:");
            return;
        }

        // 4. Rol tanlanganda
        if (text.contains("Employer (Ish beruvchi)") || text.contains("Ish beruvchi (Employer)")) {
            userRoles.put(chatId, UserRole.EMPLOYER);
        } else if (text.contains("JobSeeker (Ish izlovchi)") || text.contains("Ish izlovchi (JobSeeker)")) {
            userRoles.put(chatId, UserRole.JOB_SEEKER);
        }

        UserRole role = userRoles.getOrDefault(chatId, UserRole.NONE);

        // 5. Tegishli handlerga yo'naltirish
        if (role == UserRole.EMPLOYER) {
            SendMessage response = employerHandler.handleEmployer(message);
            if (response != null) executeMessage(response);
        } else if (role == UserRole.JOB_SEEKER) {
            SendMessage response = jobSeekerHandler.handleJobSeeker(message);
            if (response != null) executeMessage(response);
        } else {
            sendRoleSelectionMenu(chatId, "Iltimos, avval rolingizni tanlang:");
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        // CallbackQuery logikasi uchun (inline tugmalar bosilganda)
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

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}