package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class Telegram extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    public Telegram(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println("Kelgan xabar: " + text + " | ChatId: " + chatId);

            System.out.println("Kelgan xabar: " + text + " | ChatId: " + chatId);

            // 1. Javob xabarini tayyorlash
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Assalomu alaykum! Job Platform botiga xush kelibsiz.\nSiz yuborgan xabar: " + text);

            // 2. Telegram'ga javob yuborish
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }







}
