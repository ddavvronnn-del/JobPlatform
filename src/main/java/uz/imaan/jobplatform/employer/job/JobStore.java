package uz.imaan.jobplatform.employer.job;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobStore {
    private final List<JobVacancy> vacancies = new ArrayList<>();

    public void addVacancy(JobVacancy vacancy) {
        vacancies.add(vacancy);
    }

    public List<JobVacancy> getVacanciesByEmployer(Long chatId) {
        return vacancies.stream()
                .filter(v -> v.getEmployerChatId().equals(chatId))
                .collect(Collectors.toList());
    }

    public List<JobVacancy> getVacanciesByCategory(String category) {
        return vacancies.stream()
                .filter(v -> v.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
}
