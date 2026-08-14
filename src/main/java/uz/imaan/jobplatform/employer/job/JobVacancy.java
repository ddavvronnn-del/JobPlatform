package uz.imaan.jobplatform.employer.job;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "job_vacancies")
public class JobVacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employerChatId;
    private String title;
    private String category;
    private String type;           // setType() uchun
    private String salary;
    private String workHours;      // setWorkHours() uchun
    private Integer workerCount;   // setWorkerCount() uchun
    private String requirements;   // setRequirements() uchun
    private String phoneNumber;    // setPhoneNumber() uchun
    private Boolean isActive;
}