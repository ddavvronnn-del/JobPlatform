package uz.imaan.jobplatform.employer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employer_jobs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employerChatId;

    private String inn;
    private String passportSeriesNumber;
    private String fullName;
    private String phoneNumber;

    private LocalDateTime createdAt;
    private String category;
    private String jobType;
    private Double salary;
    private Integer workHours;
    private LocalDate jobDate;
    private Integer workerCount;
    private String requirements;
    private Boolean foodProvided;

    private Double latitude;
    private Double longitude;

    @Builder.Default
    private String status = "PENDING";
}
