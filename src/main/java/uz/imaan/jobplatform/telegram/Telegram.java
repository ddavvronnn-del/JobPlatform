package uz.imaan.jobplatform.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.imaan.jobplatform.employer.service.EmployerService;
import uz.imaan.jobplatform.jobseeker.service.JobSeekerService;

import java.util.ArrayList;
import java.util.List;


@Component
public class Telegram extends TelegramLongPollingBot {

    private final EmployerService employerService;
    private final JobSeekerService jobSeekerService;

    public Telegram(EmployerService employerService, JobSeekerService jobSeekerService) {
        this.employerService = employerService;
        this.jobSeekerService = jobSeekerService;
    }

    @Override
    public String getBotUsername() {
        return "JobPlatformUzBot";
    }

    @Override
    public String getBotToken() {
        return "8449248126:AAHly6vbiHKNoCUhG_uc1EU2dfuO4DB6Ycg";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        // Start bosilganda asosiy rolni tanlash menyusi
        if (message.hasText() && message.getText().equals("/start")) {
            sendRoleMenu(chatId);
            return;
        }

        // 1. Employer servisini chaqiramiz (Sizning kodingiz)
        SendMessage employerResponse = employerService.handleEmployer(message);
        if (employerResponse != null) {
            executeMessage(employerResponse);
            return;
        }

        // 2. JobSeeker servisini chaqiramiz (Jamoadoshingizning kodingiz)
        SendMessage jobSeekerResponse = jobSeekerService.handleJobSeeker(message);
        if (jobSeekerResponse != null) {
            executeMessage(jobSeekerResponse);
            return;
        }
    }

    private void sendRoleMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Xush kelibsiz! Rolni tanlang:");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Ish beruvchi (Employer)");
        row.add("Ish izlovchi (JobSeeker)");
        rows.add(row);
        keyboard.setKeyboard(rows);

        message.setReplyMarkup(keyboard);
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




