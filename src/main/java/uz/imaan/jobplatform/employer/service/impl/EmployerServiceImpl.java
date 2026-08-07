package uz.imaan.jobplatform.employer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.imaan.jobplatform.employer.dto.EmployerCreateDTO;
import uz.imaan.jobplatform.employer.dto.EmployerResponseDTO;
import uz.imaan.jobplatform.employer.dto.EmployerUpdateDTO;
import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import uz.imaan.jobplatform.employer.state.EmployerState;
import uz.imaan.jobplatform.employer.job.JobStore;
import uz.imaan.jobplatform.employer.mapper.EmployerMapper;
import uz.imaan.jobplatform.employer.repository.EmployerRepository;
import uz.imaan.jobplatform.employer.service.interfacee.EmployerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uz.imaan.jobplatform.employer.state.EmployerState.*;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements EmployerService {

    private final EmployerRepository repository;
    private final EmployerMapper mapper;
    private final JobStore jobStore;

    @Override
    public SendMessage handleEmployer(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        EmployerState currentState = jobStore.getState(chatId);

        if (text.equals("➕ Yangi e'lon yaratish")) {
            jobStore.clear(chatId);
            jobStore.setState(chatId, EmployerState.WAITING_FOR_TITLE);
            return createMessage(chatId, "📝 **Ish sarlavhasini (lavozimni) kiriting:**\n\n*Misol:* Java Backend Dasturchi");
        }

        switch (currentState) {
            case WAITING_FOR_TITLE:
                jobStore.getDraft(chatId).setTitle(text);
                jobStore.setState(chatId, EmployerState.WAITING_FOR_CATEGORY);
                return createCategoryMenu(chatId);

            case WAITING_FOR_CATEGORY:
                jobStore.getDraft(chatId).setCategory(text);
                jobStore.setState(chatId, EmployerState.WAITING_FOR_SALARY);
                return createMessage(chatId, "💰 **Maosh miqdorini kiriting:**\n\n*Misol:* 8 000 000 so'm yoki Kelishiladi");

            case WAITING_FOR_SALARY:
                jobStore.getDraft(chatId).setSalary(text);
                jobStore.setState(chatId, EmployerState.WAITING_FOR_WORK_HOURS);
                return createMessage(chatId, "⏰ **Ish vaqtini kiriting:**\n\n*Misol:* 09:00 - 18:00");

            case WAITING_FOR_WORK_HOURS:
                jobStore.getDraft(chatId).setWorkHours(text);
                jobStore.setState(chatId, EmployerState.WAITING_FOR_REQUIREMENTS);
                return createMessage(chatId, "📋 **Talablarni kiriting:**\n\n*Misol:* 1 yil tajriba, Ingliz tili");

            case WAITING_FOR_REQUIREMENTS:
                EmployerCreateDTO draft = jobStore.getDraft(chatId);
                draft.setRequirements(text);
                draft.setEmployerChatId(chatId);

                createJob(draft);
                jobStore.clear(chatId);

                return createMessage(chatId, "🎉 **E'loningiz muvaffaqiyatli e'lon qilindi!**\n\n" +
                        "📌 **Nomi:** " + draft.getTitle() + "\n" +
                        "📂 **Kategoriya:** " + draft.getCategory() + "\n" +
                        "💰 **Maosh:** " + draft.getSalary() + "\n" +
                        "⏰ **Ish vaqti:** " + draft.getWorkHours() + "\n" +
                        "📋 **Talablar:** " + draft.getRequirements());

            default:
                return createMessage(chatId, "Bo'limni tanlang:");
        }
    }

    private SendMessage createCategoryMenu(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText("📂 **Vakansiya kategoriyasini tanlang:**");

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        String[] categories = {
                "💻 IT & Dasturlash", "🏗 Qurilish & Ta'mirlash",
                "🏭 Ishlab chiqarish", "🛒 Savdo & Sotuv",
                "🍽 Restoran & Kafelar", "🚗 Transport & Haydovchi",
                "📚 Ta'lim & Repetitorlik", "🏥 Tibbiyot & Dorixona",
                "💰 Moliya & Buxgalteriya", "🛠 Xizmat ko'rsatish"
        };

        for (int i = 0; i < categories.length; i += 2) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(categories[i]));
            if (i + 1 < categories.length) {
                row.add(new KeyboardButton(categories[i + 1]));
            }
            keyboard.add(row);
        }

        markup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        sendMessage.setParseMode("Markdown");
        return sendMessage;
    }

    @Override
    @Transactional
    public EmployerResponseDTO createJob(EmployerCreateDTO dto) {
        EmployerEntity entity = mapper.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        if (entity.getStatus() == null) entity.setStatus("ACTIVE");

        EmployerEntity saved = repository.save(entity);
        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerResponseDTO getById(Long id) {
        return mapper.toResponseDTO(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("E'lon topilmadi: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> getAll() {
        return repository.findAll().stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployerResponseDTO> getByEmployerChatId(Long chatId) {
        return repository.findByEmployerChatId(chatId).stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployerResponseDTO updateJob(Long id, EmployerUpdateDTO dto) {
        EmployerEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("E'lon topilmadi: " + id));

        if (dto.getCategory() != null) entity.setCategory(dto.getCategory());
        if (dto.getSalary() != null) entity.setSalary(String.valueOf(dto.getSalary()));

        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteJob(Long id) {
        repository.deleteById(id);
    }
}