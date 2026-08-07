package uz.imaan.jobplatform.employer.job;

import org.springframework.stereotype.Component;
import uz.imaan.jobplatform.employer.dto.EmployerCreateDTO;
import uz.imaan.jobplatform.employer.state.EmployerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class JobStore {

    private final Map<Long, EmployerState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, EmployerCreateDTO> draftJobs = new ConcurrentHashMap<>();
    private final List<JobVacancy> publishedVacancies = new ArrayList<>(); // <-- E'lonlar ro'yxati

    public EmployerState getState(Long chatId) {
        return userStates.getOrDefault(chatId, EmployerState.NONE);
    }

    public void setState(Long chatId, EmployerState state) {
        userStates.put(chatId, state);
    }

    public EmployerCreateDTO getDraft(Long chatId) {
        return draftJobs.computeIfAbsent(chatId, k -> new EmployerCreateDTO());
    }

    // --- Xatoni yo'qotish uchun qo'shilgan metodlar ---

    public void addVacancy(JobVacancy vacancy) {
        publishedVacancies.add(vacancy);
    }

    public List<JobVacancy> getAllVacancies() {
        return new ArrayList<>(publishedVacancies);
    }

    public List<JobVacancy> getVacanciesByEmployer(Long employerChatId) {
        return publishedVacancies.stream()
                .filter(v -> v.getEmployerChatId() != null && v.getEmployerChatId().equals(employerChatId))
                .collect(Collectors.toList());
    }

    public void clear(Long chatId) {
        userStates.remove(chatId);
        draftJobs.remove(chatId);
    }
}