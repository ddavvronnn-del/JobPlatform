package uz.imaan.jobplatform.employer.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobVacancyRepository extends JpaRepository<JobVacancy, Long> {
    List<JobVacancy> findByEmployerChatId(Long chatId);

    List<JobVacancy> findAllByEmployerChatId(Long chatId);
    // JpaRepository o'zi avtomatik save(), findById(), delete() kabi barcha metodlarni beradi
}
