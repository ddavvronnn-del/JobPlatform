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
import uz.imaan.jobplatform.admin.dto.AdminDTO;
import uz.imaan.jobplatform.admin.service.AdminService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final Map<Long, String> userStates = new ConcurrentHashMap<>();
    private final Map<Long, AdminDTO> pendingAdmins = new ConcurrentHashMap<>();

    private String botUsername = "jobplatform_admin_bot";
    @Lazy
    @Autowired
    private  AdminService adminService;

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
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String state = userStates.getOrDefault(chatId, "IDLE");

        // 1. Старт создания
        if (text.equalsIgnoreCase("/newadmin")) {
            userStates.put(chatId, "WAITING_USERNAME");
            pendingAdmins.put(chatId, new AdminDTO());
            sendMessage(chatId, "Введите **Username** для нового админа:");
            return;
        }

        // 2. Ввод Username
        if ("WAITING_USERNAME".equals(state)) {
            pendingAdmins.get(chatId).setUsername(text);
            userStates.put(chatId, "WAITING_EMAIL");
            sendMessage(chatId, "Отлично! Теперь введите **Email**:");
            return;
        }

        // 3. Ввод Email
        if ("WAITING_EMAIL".equals(state)) {
            pendingAdmins.get(chatId).setEmail(text);
            userStates.put(chatId, "WAITING_PASSWORD");
            sendMessage(chatId, "Теперь введите **Пароль**:");
            return;
        }

        // 4. Ввод Пароля и финализация
        if ("WAITING_PASSWORD".equals(state)) {
            AdminDTO dto = pendingAdmins.get(chatId);
            dto.setPassword(text);

            try {
                adminService.createAdmin(dto);
                sendMessage(chatId, "🎉 Администратор **" + dto.getUsername() + "** успешно добавлен!");
            } catch (Exception e) {
                sendMessage(chatId, "❌ Не удалось создать админа: " + e.getMessage());
            } finally {
                // Очищаем состояние
                userStates.remove(chatId);
                pendingAdmins.remove(chatId);
            }
        }
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
        String messageText = message.getText();

        if (messageText == null) return;

        // Логирование входящих сообщений
        System.out.println(">>> ПРИШЛО СООБЩЕНИЕ. ChatId: " + chatId + ", UserId: " + userId + ", Text: " + messageText);

        // 👨‍🔧 Просмотр рабочих
        if (messageText.equals("👨‍🔧 Рабочие") || messageText.equals("👷 Рабочие") || messageText.equals("/workers")) {
            String responseText = adminService.getFormattedJobSeekersList();
            sendMessage(chatId, responseText);
            return;
        }
        // 💼 Просмотр работодателей
        else if (messageText.equals("💼 Работодатели") || messageText.equals("/employers")) {
            String responseText = adminService.getFormattedEmployersList();
            sendMessage(chatId, responseText);
            return;
        }

        // Команда /start
        if ("/start".equals(messageText)) {
            sendMessage(chatId, "Привет! Бот поиска почасовой работы JobPlatform запущен.");
        }
        // Команда /admin
        else if ("/admin".equals(messageText)) {
            if (chatId != 6326035618L && !adminService.isAdmin(chatId)) {
                sendMessage(chatId, "⛔ У вас нет прав администратора.");
                return;
            }
            sendAdminMenu(chatId);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        long userId = callbackQuery.getFrom().getId();
        long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();

        // 1. ВЫВОДИМ В КОНСОЛЬ IDE — доходит ли клик
        System.out.println(">>> НАЖАТА ИНЛАЙН-КНОПКА! Data: [" + data + "], UserId: " + userId);

        // 2. Гасим часики на кнопке
        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            execute(answer);
        } catch (Exception e) {
            System.err.println("Ошибка при гашении часиков: " + e.getMessage());
        }

        // 3. Отправляем простой ответ БЕЗ Markdown (чтобы исключить ошибки форматирования)
        try {
            AdminDTO stats = adminService.getStats();

            String simpleText = "Статистика платформы:\n" +
                    "Админов: " + stats.getTotalAdmins() + "\n" +
                    "Работодателей: " + stats.getTotalEmployers() + "\n" +
                    "Исполнителей: " + stats.getTotalWorkers();

            sendMessage(chatId, simpleText);

        } catch (Exception e) {
            System.err.println("❌ Ошибка при выполнении getStats():");
            e.printStackTrace();
            sendMessage(chatId, "❌ Произошла ошибка на сервере при получении статистики.");
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

    public  void sendMessage(long chatId, String text) {
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

    private  void executeMessage(SendMessage message) {
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

    // Пример обработки команд/кнопок в боте:


}