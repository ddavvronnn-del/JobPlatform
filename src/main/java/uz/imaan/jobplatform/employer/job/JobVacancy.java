package uz.imaan.jobplatform.employer.job;

import jakarta.persistence.*;
import lombok.Data;

@Entity // 👈 ДОБАВЬ ЭТУ СТРОЧКУ!
@Table(name = "job_vacancies")
@Data
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

    public Long getEmployerChatId() { return employerChatId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getSalary() { return salary; }

}
