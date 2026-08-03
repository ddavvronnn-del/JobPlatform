package uz.imaan.jobplatform.admin;


import org.springframework.beans.factory.annotation.Value;
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

    private final String botUsername;
    private final AdminService adminService;

    public TelegramBotService(
            @Value("${8470148420:AAEcjcwI9sKspY94OFiB7V5gqmAjQvgVhPY}") String botToken,
            @Value("${jobplatform_admin_bot}") String botUsername,
            AdminService adminService) {
        super(botToken);
        this.botUsername = botUsername;
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

    // Обработка текстовых сообщений
    private void handleTextMessage(Message message) {
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String text = message.getText();

        if ("/start".equals(text)) {
            sendTextMessage(chatId, "Привет! Бот поиска почасовой работы JobPlatform запущен.");
        } else if ("/admin".equals(text)) {
            if (!adminService.isAdmin(userId)) {
                sendTextMessage(chatId, "⛔ У вас нет прав администратора.");
                return;
            }
            sendAdminMenu(chatId);
        }
    }

    // Обработка нажатий инлайн-кнопок
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();

        if (!adminService.isAdmin(userId)) {
            sendTextMessage(chatId, "⛔ У вас нет доступа к админ-панели.");
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
        // Проверка команды на блокировку
        if (text.startsWith("/block")) {
            String[] parts = text.split(" ", 3); // Разбиваем на: [/block, userId, reason]

            if (parts.length < 3) {
                // Если админ ввел неполную команду
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

                // Вызываем ваш метод блокировки
                blockUser(blockDTO);

                // Уведомление админу об успешном действии
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
        }

        // Проверка команды на разблокировку
        else if (text.startsWith("/unblock")) {
            String[] parts = text.split(" ", 2); // [/unblock, userId]

            if (parts.length < 2) {
                executeMessage(SendMessage.builder()
                        .chatId(adminChatId.toString())
                        .text("⚠️ Формат команды: /unblock <ID_пользователя>")
                        .build());
                return;
            }

            try {
                Long targetUserId = Long.parseLong(parts[1]);

                // Вызываем ваш метод разблокировки
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

    // Отправка меню админа
    private void sendAdminMenu(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("⚙️ *Панель администратора JobPlatform*\nВыберите действие:")
                .parseMode("Markdown")
                .replyMarkup(getAdminKeyboard())
                .build();
        executeMessage(message);
    }

    // Клавиатура админки
    private InlineKeyboardMarkup getAdminKeyboard() {
        InlineKeyboardButton statsBtn = InlineKeyboardButton.builder()
                .text("📊 Статистика платформы")
                .callbackData("admin_stats")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(statsBtn))
                .build();
    }

    // Вспомогательные методы отправки
    private void sendTextMessage(long chatId, String text) {
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
            e.printStackTrace();
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
        // 1. Вызываем логику блокировки в БД через AdminService
        adminService.blockUser(blockDTO);

        // 2. Отправляем уведомление заблокированному пользователю в Telegram
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
        // 1. Снимаем блокировку в БД
        adminService.unblockUser(userId);

        // 2. Отправляем уведомление пользователю
        String unblockMessage = "✅ Ваш аккаунт успешно разблокирован! Вы снова можете пользоваться ботом.";

        SendMessage sendMessage = SendMessage.builder()
                .chatId(userId.toString())
                .text(unblockMessage)
                .build();

        executeMessage(sendMessage);
    }
}
