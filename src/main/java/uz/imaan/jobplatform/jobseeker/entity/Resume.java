package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity  // ⬅️ BU ANNOTATSIYA BO'LISHI SHART!
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // Rezyume ID si (avtomatik)

    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;               // Qaysi ish izlovchiga tegishli

    @Column(name = "title", nullable = false)
    private String title;                   // Rezyume sarlavhasi (mas: "Java Developer")

    @Column(name = "full_name", nullable = false)
    private String fullName;                // To'liq ism

    @Column(name = "email")
    private String email;                   // Email manzil

    @Column(name = "phone_number")
    private String phoneNumber;             // Telefon raqam

    @Column(name = "profession")
    private String profession;              // Kasbi (mas: "Dasturchi")

    @Column(name = "experience", length = 2000)
    private String experience;              // Ish tajribasi (mas: "3 yil Java da ishlagan")

    @Column(name = "education", length = 1000)
    private String education;               // Ta'lim (mas: "TATU, 2020-2024")

    @Column(name = "skills", length = 1000)
    private String skills;                  // Ko'nikmalar (mas: "Java, Spring, SQL")

    @Column(name = "about", length = 2000)
    private String about;                   // O'zi haqida qisqacha

    @Column(name = "is_active")
    private Boolean isActive = true;        // Aktivmi? (default: true)

    @Column(name = "created_at")
    private LocalDateTime createdAt;        // Yaratilgan vaqt

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;        // Yangilangan vaqt

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
