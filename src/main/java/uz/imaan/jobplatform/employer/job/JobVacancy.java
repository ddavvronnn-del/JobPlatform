package uz.imaan.jobplatform.employer.job;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_vacancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobVacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employerChatId;
    private String title;
    private String category;
    private String type;
    private String salary;

    public JobVacancy(Long employerChatId, String title, String category, String type, String salary) {
        this.employerChatId = employerChatId;
        this.title = title;
        this.category = category;
        this.type = type;
        this.salary = salary;
    }
}