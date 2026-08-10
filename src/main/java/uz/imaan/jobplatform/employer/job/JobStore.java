package uz.imaan.jobplatform.employer.job;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.imaan.jobplatform.employer.dto.EmployerCreateDTO;
import uz.imaan.jobplatform.employer.state.EmployerState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor  // ✅ Repository ni constructor orqali oladi
public class JobStore {

    private final JobVacancyRepository vacancyRepository;  // ✅ Repository qo'shildi

    private final Map<Long, EmployerState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, EmployerCreateDTO> draftJobs = new ConcurrentHashMap<>();

    public EmployerState getState(Long chatId) {
        return userStates.getOrDefault(chatId, EmployerState.NONE);
    }

    public void setState(Long chatId, EmployerState state) {
        userStates.put(chatId, state);
    }

    public EmployerCreateDTO getDraft(Long chatId) {
        return draftJobs.computeIfAbsent(chatId, k -> new EmployerCreateDTO());
    }

    // ============================================
    // VAKANSIYA BILAN ISHLASH (Repository orqali)
    // ============================================

    public void addVacancy(JobVacancy vacancy) {
        vacancyRepository.save(vacancy);  // ✅ Bazaga saqlaydi
        System.out.println("✅ Vakansiya saqlandi: " + vacancy.getTitle());
    }

    public List<JobVacancy> getAllVacancies() {
        return vacancyRepository.findAll();  // ✅ Bazadan o'qiydi
    }

    public List<JobVacancy> getVacanciesByEmployer(Long employerChatId) {
        return vacancyRepository.findByEmployerChatId(employerChatId);  // ✅ Bazadan o'qiydi
    }

    public void deleteVacancy(Long id) {
        vacancyRepository.deleteById(id);
    }

    public void clear(Long chatId) {
        userStates.remove(chatId);
        draftJobs.remove(chatId);
    }
}