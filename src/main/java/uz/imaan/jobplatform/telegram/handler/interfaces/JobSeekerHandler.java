package uz.imaan.jobplatform.telegram.handler.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;

import java.util.Optional;

public interface JobSeekerHandler {

    // ============================================
    // 1. ASOSIY HANDLE METODI
    // ============================================
    SendMessage handleJobSeeker(Message message);


    // ============================================
    // 2. TIL BILAN ISHLASH
    // ============================================
    void updateLanguage(Long chatId, String languageCode);


    // ============================================
    // 3. PROFIL METODLARI
    // ============================================
    SendMessage showProfile(Long chatId, Optional<JobSeekerProfile> profileOpt);

    String getProfileInfo(JobSeekerProfile profile, Optional<JobSeekerProfile> profileOpt);

    SendMessage handleProfileMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt);


    // ============================================
    // 4. HAMYON METODLARI
    // ============================================
    SendMessage showWallet(Long chatId, Optional<JobSeekerProfile> profileOpt);

    SendMessage handleWalletMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt);

    // ✅ YANGI: KARTANI O'CHIRISH
    SendMessage handleDeleteCard(Long chatId, Optional<JobSeekerProfile> profileOpt);


    // ============================================
    // 5. ARIZALAR METODLARI
    // ============================================
    SendMessage handleShowApplications(Long chatId, Optional<JobSeekerProfile> profileOpt);


    // ============================================
    // 6. SOZLAMALAR METODLARI
    // ============================================
    SendMessage handleSettingsMenu(Long chatId, String text, Optional<JobSeekerProfile> profileOpt);

    SendMessage handleLanguageSelection(Long chatId, String text, Optional<JobSeekerProfile> profileOpt);


    // ============================================
    // 7. KARTA QO'SHISH METODLARI
    // ============================================
    SendMessage handleCardNumber(Long chatId, String text, Optional<JobSeekerProfile> profileOpt);

    SendMessage handleCardHolder(Long chatId, String text, Optional<JobSeekerProfile> profileOpt);


    // ============================================
    // 8. KEYBOARD METODLARI
    // ============================================
    InlineKeyboardMarkup getCategoryInlineKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getMainMenuKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getJobTypeKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getRegistrationCategoryKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getSearchCategoryKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getProfileKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getWalletKeyboard(Optional<JobSeekerProfile> profileOpt);

    // ✅ YANGI: KARTA BOR/YO'Q BO'YICHA KEYBOARD
    ReplyKeyboardMarkup getWalletKeyboard(Optional<JobSeekerProfile> profileOpt, boolean hasCard);

    ReplyKeyboardMarkup getSettingsKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getLanguageKeyboard();

    ReplyKeyboardMarkup getActiveJobsKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getJobActionKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getCancelKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getSubBackKeyboard(Optional<JobSeekerProfile> profileOpt);

    ReplyKeyboardMarkup getPhoneKeyboard();


    // ============================================
    // 9. KATEGORIYA BO'YICHA QIDIRISH
    // ============================================
    void handleCategorySearch(Long chatId, String categoryKey, Optional<JobSeekerProfile> profileOpt);
}