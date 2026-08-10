package uz.imaan.jobplatform.admin;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.imaan.jobplatform.admin.dto.AdminDTO;
import uz.imaan.jobplatform.admin.service.AdminService;

import java.util.List;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private String botUsername = "jobplatform_admin_bot";
    private final AdminService adminService;

    public TelegramBotService(AdminService adminService) {
//        super("8470148420:AAEcjcwI9sKspY94OFiB7V5gqmAjQvgVhPY");
        super("8757778609:AAEtyutp-PvYDx8DbtcIYSQmw7W4hU2GryI");
        this.botUsername = "employment_chirchik_bot";

//        this.botUsername = "jobplatform_admin_bot";
        this.adminService = adminService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при обработке сообщения от Telegram:");
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Message message) {
        
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String text = message.getText();

        if ("/start".equals(text)) {
            sendMessage(chatId, "Привет! Бот поиска почасовой работы JobPlatform запущен.");
        } else if ("/admin".equals(text)) {
            // Если это ваш Telegram ID ИЛИ пользователь является админом в базе
            if (chatId != 6326035618L && !adminService.isAdmin(chatId)) {
                sendMessage(chatId, "⛔ У вас нет прав администратора.");
                return;
            }
            sendAdminMenu(chatId);
        
        }
        System.out.println(">>> ПРИШЛО СООБЩЕНИЕ. ChatId: " + chatId + ", UserId: " + userId); // <--- ДОБАВИТЬ ЭТУ СТРОКУ
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        if (!adminService.isAdmin(userId)) {
            sendMessage(chatId, "⛔ У вас нет доступа к админ-панели.");
            return;
        }

        if ("admin_stats".equals(data)) {
            AdminDTO stats = adminService.getStats();
            String statsText = String.format(
                    "📊 *Статистика платформы:*\n\n" +
                            "👥 Всего админов: `%d`\n" +
                            "🏢 Работодателей: `%d`\n" +
                            "👷‍♂️ Исполнителей: `%d`\n" +
                            "💼 Всего вакансий: `%d`\n" +
                            "⚡ Активных смен: `%d`\n" +
                            "✅ Завершенных смен: `%d`",
                    stats.getTotalAdmins(),
                    stats.getTotalEmployers(),
                    stats.getTotalWorkers(),
                    stats.getTotalJobs(),
                    stats.getActiveJobs(),
                    stats.getCompletedJobs()
            );

            editMessageText(chatId, messageId, statsText, getAdminKeyboard());
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

                AdminDTO blockDTO = new AdminDTO();
                blockDTO.setUserId(targetUserId);
                blockDTO.setReason(reason);

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

    private void sendAdminMenu(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("⚙️ *Панель администратора JobPlatform*\nВыберите действие:")
                .parseMode("Markdown")
                .replyMarkup(getAdminKeyboard())
                .build();
        executeMessage(message);
    }

    private InlineKeyboardMarkup getAdminKeyboard() {
        InlineKeyboardButton statsBtn = InlineKeyboardButton.builder()
                .text("📊 Статистика платформы")
                .callbackData("admin_stats")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(statsBtn))
                .build();
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
            execute(message); // 👈 Замени editMessageText на message
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

    public void blockUser(AdminDTO blockDTO) {
        adminService.blockUser(blockDTO);

        String blockMessage = String.format(
                "❌ Ваш аккаунт заблокирован.\nПричина: %s",
                blockDTO.getReason()
        );

        SendMessage sendMessage = SendMessage.builder()
                .chatId(blockDTO.getUserId().toString())
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
    public void notifyAdmin(String notificationText) {
        Long adminTelegramId = 6326035618L ;
        SendMessage message = SendMessage.builder()
                .chatId(adminTelegramId.toString())
                .text(notificationText)
                .parseMode("Markdown")
                .build();
        try {
            execute(message); // Здесь execute() работает, потому что класс наследует TelegramLongPollingBot
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}