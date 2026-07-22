package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;

    @Column(name = "cover_letter", length = 1000)
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ApplicationStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }


}
