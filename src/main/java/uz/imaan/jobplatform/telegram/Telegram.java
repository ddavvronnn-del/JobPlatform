package uz.imaan.jobplatform.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.imaan.jobplatform.employer.controller.EmployerController;
import uz.imaan.jobplatform.employer.service.EmployerService;

import java.util.ArrayList;
import java.util.List;


@Component
public class Telegram extends TelegramLongPollingBot {

    private final String botUsername = "@JobPlatformUzBot";

    public Telegram() {
        super("8449248126:AAHly6vbiHKNoCUhG_uc1EU2dfuO4DB6Ycg");
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


            if (text.equals("/start") || text.equals("Asosiy menyu")) {
                sendRoleSelectionMenu(chatId, "Hush kelibsiz! Rolingizni tanlang:");
            }

            // 2. EMPLOYER tugmasi bosilganda
            else if (text.equals("Employer (Ish beruvchi)")) {
                sendEmployerMenu(chatId, "Siz Ish beruvchi menyusidasiz. Kerakli bo'limni tanlang:");
            }

            // 3. JOBSEEKER tugmasi bosilganda
            else if (text.equals("JobSeeker (Ish izlovchi)")) {
                sendJobSeekerMenu(chatId, "Siz Ish izlovchi menyusidasiz. Kerakli bo'limni tanlang:");
            }

            // 4. EMPLOYER MENYU TUGMALARI ISHLASHI (Sizning serviceringiz chaqiriladi)
            else if (text.equals("Mening e'lonlarim")) {
                // EmployerService dagi metodni chaqirib e'lonlarni olasiz
                EmployerService employerService = null;
                var jobs = employerService.getByEmployerChatId(chatId);
                if (jobs.isEmpty()) {
                    sendMessage(chatId, "Sizda hali yaratilgan e'lonlar yo'q.");
                } else {
                    sendMessage(chatId, "Sizning e'lonlaringiz soni: " + jobs.size());
                }
            }
            else if (text.equals("Yangi e'lon yaratish")) {
                sendMessage(chatId, "E'lon yaratish uchun ma'lumotlarni kiriting (INN va Pasport seriyasi):");
                // Bu yerda FSM (Finite State Machine) yoki ketma-ketlik mantiqini qilasiz
            }

            // 5. JOBSEEKER MENYU TUGMALARI ISHLASHI (Jamoadoshingiz servisi chaqiriladi)
            else if (text.equals("Ishlarni ko'rish")) {
                // jobSeekerService.getAllJobs()...
                sendMessage(chatId, "Mavjud bo'sh ish o'rinlari ro'yxati...");
            }
            else if (text.equals("Mening arizalarim")) { // <-- SHU QISMINI QO'SHING
                sendMessage(chatId, "Siz topshirgan arizalar ro'yxati (tez orada ulanadi)...");
            }
        }
    }

    // --- TUGMALARNI CHIQARUVCHI YORDAMCHI METODLAR ---

    // Asosiy Rol Tanlash Menyusi (Employer / JobSeeker)
    private void sendRoleSelectionMenu(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

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

    // Employer (Ish beruvchi) Menyusi
    private void sendEmployerMenu(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Yangi e'lon yaratish"));
        row1.add(new KeyboardButton("Mening e'lonlarim"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Asosiy menyu"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    // JobSeeker (Ish izlovchi) Menyusi
    private void sendJobSeekerMenu(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Ishlarni ko'rish"));
        row1.add(new KeyboardButton("Mening arizalarim"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Asosiy menyu"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    // Oddiy matnli xabar yuborish
    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
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




