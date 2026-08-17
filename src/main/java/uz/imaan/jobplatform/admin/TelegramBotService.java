package uz.imaan.jobplatform.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.imaan.jobplatform.admin.dto.AdminDtoTwo;
import uz.imaan.jobplatform.admin.service.AdminService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    // Хранение языка для каждого пользователя (chatId -> "RU" или "UZ")
    private final Map<Long, String> userLanguages = new ConcurrentHashMap<>();

    private String getLanguage(long chatId) {
        return userLanguages.getOrDefault(chatId, "RU"); // По умолчанию Русский
    }

    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private final Map<Long, AdminDtoTwo> pendingAdmins = new ConcurrentHashMap<>();

    private String botUsername = "jobplatform_admin_bot";

    @Lazy
    @Autowired
    private AdminService adminService;

    public TelegramBotService(AdminService adminService) {
        super("8757778609:AAEtyutp-PvYDx8DbtcIYSQmw7W4hU2GryI");
        this.botUsername = "employment_chirchik_bot";
        this.adminService = adminService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            // Обработка текстовых сообщений
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();

                // ИГНОРИРУЕМ ПОВТОРНЫЕ ОБНОВЛЕНИЯ
                if (lastUpdateId >= update.getUpdateId()) {
                    return;
                }
                lastUpdateId = update.getUpdateId();

                handleTextMessage(message);
                return;
            }

            // Обработка нажатий на кнопки
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long lastUpdateId = 0;

    private void handleTextMessage(Message message) {
        String text = message.getText();
        long chatId = message.getChatId();

        if (text.startsWith("/addadmin")) {
            handleAddAdminCommand(text, chatId);
            return;
        }

        if (text == null) return;

        // ===== ОБРАБОТКА КОМАНД =====
        if (text.startsWith("/")) {

            // 1. КОМАНДА /START
            if ("/start".equals(text)) {
                if (!userLanguages.containsKey(chatId)) {
                    userLanguages.put(chatId, "RU");
                }
                sendMessage(chatId, "Привет! Бот поиска почасовой работы JobPlatform запущен.");
                return;
            }

            // 2. КОМАНДА /ADMIN
            // 2. КОМАНДА /ADMIN
            if ("/admin".equals(text)) {
                if (!adminService.isAdmin(chatId)) { // ✅ Исправлено: проверяет базу и список adminIds
                    sendMessage(chatId, "У вас нет прав администратора.");
                    return;
                }
                sendAdminMenu(chatId);
                return;
            }

            // 3. КОМАНДЫ БЛОКИРОВКИ И РАЗБЛОКИРОВКИ
            if (text.startsWith("/block") || text.startsWith("/unblock")) {
                handleAdminCommand(text, chatId);
                return;
            }
        }

        System.out.println("Игнорируем текст: " + text);
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();

        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            execute(answer);
        } catch (Exception ignored) {}

        if ("change_language".equals(data)) {
            sendLanguageMenu(chatId);
        } else if ("lang_ru".equals(data)) {
            userLanguages.put(chatId, "RU");
            sendMessage(chatId, "✅ Язык успешно изменён на **Русский**!");
            sendAdminMenu(chatId);
        } else if ("lang_uz".equals(data)) {
            userLanguages.put(chatId, "UZ");
            sendMessage(chatId, "✅ Til **O'zbekcha**ga muvaffaqiyatli o'zgartirildi!");
            sendAdminMenu(chatId);
        } else if ("admin_stats".equals(data) || "stats".equals(data)) {
            AdminDtoTwo stats = adminService.getStats();
            String lang = getLanguage(chatId);

            String statsText = "UZ".equals(lang) ?
                    String.format("📊 *Platforma statistikasi:*\n\n👥 Adminlar: `%d`\n🏢 Ish beruvchilar: `%d`\n👤 Ishchilar: `%d`",
                            stats.totalAdmins(), stats.totalEmployers(), stats.totalWorkers()) :
                    String.format("📊 *Статистика платформы:*\n\n👥 Админов: `%d`\n🏢 Работодателей: `%d`\n👤 Исполнителей: `%d`",
                            stats.totalAdmins(), stats.totalEmployers(), stats.totalWorkers());

            sendMessage(chatId, statsText);
        } else if ("admin_workers".equals(data) || "workers".equals(data)) {
            sendMessage(chatId, adminService.getFormattedJobSeekersList());
        } else if ("admin_employers".equals(data) || "employers".equals(data)) {
            sendMessage(chatId, adminService.getFormattedEmployersList());
        }
    }

    public void handleAdminCommand(String text, Long adminChatId) {
        if (text.startsWith("/block")) {
            String[] parts = text.split(" ", 3);

            if (parts.length < 3) {
                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("⚠️ Формат команды: /block <ID_пользователя> <Причина>")
                        .build());
                return;
            }

            try {
                Long targetUserId = Long.parseLong(parts[1]);
                String reason = parts[2];

                AdminDtoTwo blockDTO = new AdminDtoTwo(
                        null, null, null, null, null, null, null, null, null,
                        0L, null, null, null, targetUserId, reason, null, null, null, 0L, 0L
                );

                blockUser(blockDTO);

                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("✅ Пользователь " + targetUserId + " успешно заблокирован.")
                        .build());

            } catch (NumberFormatException e) {
                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("❌ Неверный ID пользователя. ID должен состоять только из цифр.")
                        .build());
            }
        } else if (text.startsWith("/unblock")) {
            String[] parts = text.split(" ", 2);

            if (parts.length < 2) {
                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("⚠️ Формат команды: /unblock <ID_пользователя>")
                        .build());
                return;
            }

            try {
                Long targetUserId = Long.parseLong(parts[1]);

                unblockUser(targetUserId);

                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("✅ Пользователь " + targetUserId + " разблокирован.")
                        .build());

            } catch (NumberFormatException e) {
                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("❌ Неверный ID пользователя.")
                        .build());
            }
        }
    }

    public void sendAdminMenu(long chatId) {
        String lang = getLanguage(chatId);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("UZ".equals(lang)
                ? "⚙️ **JobPlatform Admin Paneli**\nBo'limni tanlang:"
                : "⚙️ **Панель администратора JobPlatform**\nВыберите действие:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton statsBtn = new InlineKeyboardButton();
        statsBtn.setText("UZ".equals(lang) ? "📊 Platforma statistikasi" : "📊 Статистика платформы");
        statsBtn.setCallbackData("admin_stats");

        InlineKeyboardButton workersBtn = new InlineKeyboardButton();
        workersBtn.setText("UZ".equals(lang) ? "👷 Ishchilar" : "👷 Рабочие");
        workersBtn.setCallbackData("admin_workers");

        InlineKeyboardButton employersBtn = new InlineKeyboardButton();
        employersBtn.setText("UZ".equals(lang) ? "🏢 Ish beruvchilar" : "🏢 Работодатели");
        employersBtn.setCallbackData("admin_employers");

        InlineKeyboardButton langBtn = new InlineKeyboardButton();
        langBtn.setText("🌐 Сменить язык / Tilni o'zgartirish");
        langBtn.setCallbackData("change_language");

        rows.add(List.of(statsBtn));
        rows.add(List.of(workersBtn, employersBtn));
        rows.add(List.of(langBtn));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build();
        executeMessage(message);
    }

    private void editMessageText(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText message = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(keyboard)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void blockUser(AdminDtoTwo blockDTO) {
        adminService.blockUser(blockDTO);

        String blockMessage = String.format(
                "❌ Ваш аккаунт заблокирован.\nПричина: %s",
                blockDTO.reason()
        );

        SendMessage sendMessage = SendMessage.builder()
                .chatId(blockDTO.userId().toString())
                .text(blockMessage)
                .build();

        executeMessage(sendMessage);
    }

    public void unblockUser(Long userId) {
        adminService.unblockUser(userId);

        String unblockMessage = "✅ Ваш аккаунт успешно разблокирован! Вы снова можете пользоваться ботом.";

        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId.toString())
                .text(unblockMessage)
                .build();

        executeMessage(sendMessage);
    }

    private void sendLanguageMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("🌐 **Выберите язык / Tilni tanlang:**");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton ruBtn = new InlineKeyboardButton();
        ruBtn.setText("🇷🇺 Русский");
        ruBtn.setCallbackData("lang_ru");

        InlineKeyboardButton uzBtn = new InlineKeyboardButton();
        uzBtn.setText("🇺🇿 O'zbekcha");
        uzBtn.setCallbackData("lang_uz");

        rows.add(List.of(ruBtn, uzBtn));
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleAddAdminCommand(String text, long chatId) {
        if (!adminService.isAdmin(chatId)) {
            sendMessage(chatId, "❌ У вас нет прав для выполнения этой команды.");
            return;
        }

        String[] parts = text.split("\\s+");
        if (parts.length < 2) {
            sendMessage(chatId, "⚠️ Формат команды: `/addadmin <Telegram_ID>`");
            return;
        }

        try {
            Long newAdminId = Long.parseLong(parts[1]);

            AdminDtoTwo newAdminDto = new AdminDtoTwo(
                    null, null, null, null, null, null, null, null, null,
                    newAdminId, null, null, null, null, null, null, null, null, 0L, 0L
            );
            adminService.createAdmin(newAdminDto);

            sendMessage(chatId, "✅ Пользователь с ID `" + newAdminId + "` успешно назначен администратором.");
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Некорректный Telegram ID. ID должен состоять только из цифр.");
        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка при добавлении администратора: " + e.getMessage());
        }
    }
}