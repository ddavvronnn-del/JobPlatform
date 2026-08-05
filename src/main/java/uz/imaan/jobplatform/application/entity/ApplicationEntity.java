package uz.imaan.jobplatform.application.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.imaan.jobplatform.application.enums.ApplicationStatus;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vacancyId;
    private String vacancyTitle;

    private Long jobSeekerTelegramId;
    private Long employerTelegramId;

    private String candidateName;
    private String phone;
    private String note;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
}