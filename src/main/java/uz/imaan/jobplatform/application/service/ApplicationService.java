package uz.imaan.jobplatform.application.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.imaan.jobplatform.application.entity.ApplicationEntity;
import java.util.List;

public class ApplicationService {
    public SendMessage buildEmployerNotification(ApplicationEntity app) {
        String text = String.format("""
            📥 <b>Vakansiyangizga yangi ariza keldi!</b>
            
            📌 <b>Vakansiya:</b> %s
            👤 <b>Nomzod:</b> %s
            📞 <b>Tel:</b> %s
            💡 <b>Izoh:</b> %s
            """,
                app.getVacancyTitle(), app.getCandidateName(), app.getPhone(), app.getNote());

        SendMessage message = SendMessage.builder()
                .chatId(app.getEmployerTelegramId().toString())
                .text(text)
                .parseMode("HTML")
                .build();

        InlineKeyboardButton acceptBtn = InlineKeyboardButton.builder()
                .text("✅ Qabul qilish")
                .callbackData("ACCEPT_APP_" + app.getId())
                .build();

        InlineKeyboardButton rejectBtn = InlineKeyboardButton.builder()
                .text("❌ Rad etish")
                .callbackData("REJECT_APP_" + app.getId())
                .build();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(acceptBtn, rejectBtn)));
        message.setReplyMarkup(markup);

        return message;
    }
}
