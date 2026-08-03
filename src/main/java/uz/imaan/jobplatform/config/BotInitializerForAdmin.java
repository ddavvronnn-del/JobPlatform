package uz.imaan.jobplatform.config;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.imaan.jobplatform.admin.TelegramBotService;

@Component

public class BotInitializerForAdmin {

    private final TelegramBotService botService;

    // В конструкторе используем TelegramBotService вместо Telegram
    public BotInitializerForAdmin(TelegramBotService botService) {
        this.botService = botService;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(botService);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
