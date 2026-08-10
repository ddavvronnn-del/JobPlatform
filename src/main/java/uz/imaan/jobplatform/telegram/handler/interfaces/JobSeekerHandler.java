package uz.imaan.jobplatform.telegram.handler.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;

import java.util.Optional;

public interface JobSeekerHandler {

    // ASOSIY HANDLE METODI

    SendMessage handleJobSeeker(Message message);


    // TIL BILAN ISHLASH

    void updateLanguage(Long chatId, String languageCode);


    // KEYBOARD METODLARI

    InlineKeyboardMarkup getCategoryInlineKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getMainMenuKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getJobTypeKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getRegistrationCategoryKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getSearchCategoryKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getProfileKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getWalletKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getSettingsKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getLanguageKeyboard();

    ReplyKeyboardMarkup getActiveJobsKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getJobActionKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getCancelKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getSubBackKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getPhoneKeyboard();


    // KATEGORIYA BO'YICHA QIDIRISH

    void handleCategorySearch(Long chatId, String categoryKey, Optional<JobSeekerProfile> profileOpt);

}
