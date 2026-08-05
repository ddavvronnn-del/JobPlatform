package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import uz.imaan.jobplatform.application.entity.ApplicationEntity;
import uz.imaan.jobplatform.application.enums.ApplicationStatus;
import uz.imaan.jobplatform.application.repository.ApplicationRepository;

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
    private final ApplicationRepository applicationRepository;

    // Har bir chatId bo'yicha rolini eslab qolamiz
    private final Map<Long, UserRole> userRoles = new ConcurrentHashMap<>();

    @Autowired
    public Telegram(JobSeekerHandler jobSeekerHandler,
                    EmployerHandler employerHandler,
                    ApplicationRepository applicationRepository) {
        super("8449248126:AAFPgTpsBD2o1k_cp8YbG8_wqp9o8KnRCss");
        /*super("8748423781:AAGfMc3bihKxmH5QeLN8EAekSrUNmFy3YQo");*/

        this.jobSeekerHandler = jobSeekerHandler;
        this.employerHandler = employerHandler;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public String getBotUsername() {
        return "@JobPlatformUzBot";
      /*  return "@Rezyume_bot";*/

    }

    @Override
    public void onUpdateReceived(Update update) {
        // 1. Inline tugmalar (CallbackQuery) bosilganda
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.hasText() ? message.getText() : "";

        // 2. /start yoki "Asosiy menyu" bosilsa
        if (text.equals("/start") || text.equals("Asosiy menyu")) {
            userRoles.put(chatId, UserRole.NONE);
            sendRoleSelectionMenu(chatId, "Xush kelibsiz! Rolingizni tanlang:");
            return;
        }

        // 3. Rol tanlanganda userRoles xaritasida saqlaymiz
        if (text.contains("Employer (Ish beruvchi)") || text.contains("Ish beruvchi (Employer)")) {
            userRoles.put(chatId, UserRole.EMPLOYER);
        } else if (text.contains("JobSeeker (Ish izlovchi)") || text.contains("Ish izlovchi (JobSeeker)")) {
            userRoles.put(chatId, UserRole.JOB_SEEKER);
        }

        UserRole role = userRoles.getOrDefault(chatId, UserRole.NONE);

        // 4. Tanlangan roliga muvofiq Xabar FAQAT tegishli handlerga boradi
        if (role == UserRole.EMPLOYER) {
            SendMessage response = employerHandler.handleEmployer(message);
            if (response != null) executeMessage(response);
        } else if (role == UserRole.JOB_SEEKER) {
            SendMessage response = jobSeekerHandler.handleJobSeeker(message);
            if (response != null) executeMessage(response);
        } else {
            // Rol hali tanlanmagan bo'lsa
            sendRoleSelectionMenu(chatId, "Iltimos, avval rolingizni tanlang:");
        }
    }

    // Callback query (Inline tugmalar) ni qayta ishlash
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long employerChatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        if (data.startsWith("ACCEPT_APP_")) {
            Long appId = Long.parseLong(data.replace("ACCEPT_APP_", ""));
            processApplicationResponse(appId, ApplicationStatus.ACCEPTED, employerChatId, messageId, "✅ Qabul qilindi");
        } else if (data.startsWith("REJECT_APP_")) {
            Long appId = Long.parseLong(data.replace("REJECT_APP_", ""));
            processApplicationResponse(appId, ApplicationStatus.REJECTED, employerChatId, messageId, "❌ Rad etildi");
        }
    }

    // Ariza holatini yangilash va javob yuborish
    private void processApplicationResponse(Long appId, ApplicationStatus status, Long employerChatId, Integer messageId, String statusLabel) {
        ApplicationEntity app = applicationRepository.findById(appId).orElse(null);
        if (app == null) return;

        // Bazada statusni yangilash
        app.setStatus(status);
        applicationRepository.save(app);

        // 1. Ish beruvchidagi xabardan tugmalarni olib tashlash va holatni yangilash
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(employerChatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setParseMode("HTML");
        editMessage.setText(String.format("""
                📥 <b>Vakansiyaga kelgan ariza</b>
                
                📌 <b>Vakansiya:</b> %s
                👤 <b>Nomzod:</b> %s
                📞 <b>Tel:</b> %s
                💡 <b>Izoh:</b> %s
                
                <b>Holat:</b> %s
                """, app.getVacancyTitle(), app.getCandidateName(), app.getPhone(), app.getNote(), statusLabel));

        // 2. Ish izlovchiga (JobSeeker) bildirishnoma yuborish
        SendMessage notifySeeker = new SendMessage();
        notifySeeker.setChatId(app.getJobSeekerTelegramId().toString());
        notifySeeker.setParseMode("HTML");
        if (status == ApplicationStatus.ACCEPTED) {
            notifySeeker.setText("🎉 <b>Tabriklaymiz!</b> Sizning '" + app.getVacancyTitle() + "' vakansiyasiga topshirgan arizangiz qabul qilindi.");
        } else {
            notifySeeker.setText("😔 Sizning '" + app.getVacancyTitle() + "' vakansiyasiga topshirgan arizangiz rad etildi.");
        }

        try {
            execute(editMessage);
            execute(notifySeeker);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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