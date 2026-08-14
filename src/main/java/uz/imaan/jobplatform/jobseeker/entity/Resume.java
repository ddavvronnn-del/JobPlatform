package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "Ish izlovchi ID si kiritilishi shart")
    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;               // Qaysi ish izlovchiga tegishli

    @NotBlank(message = "Sarlavha kiritilishi shart")
    @Size(min = 3, max = 100, message = "Sarlavha 3-100 belgidan iborat bo'lishi kerak")
    @Column(name = "title", nullable = false)
    private String title;                   // Rezyume sarlavhasi (mas: "Java Developer")

    @NotBlank(message = "Ism-familiya kiritilishi shart")
    @Size(min = 3, max = 100, message = "Ism-familiya 3-100 belgidan iborat bo'lishi kerak")
    @Column(name = "full_name", nullable = false)
    private String fullName;                // To'liq ism

    @Email(message = "Email noto'g'ri formatda")
    @Column(name = "email")
    private String email;                   // Email manzil

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Telefon raqam noto'g'ri formatda")
    @Column(name = "phone_number")
    private String phoneNumber;             // Telefon raqam

    @Size(max = 100, message = "Kasb 100 belgidan oshmasligi kerak")
    @Column(name = "profession")
    private String profession;              // Kasbi (mas: "Dasturchi")

    @Size(max = 2000, message = "Tajriba 2000 belgidan oshmasligi kerak")
    @Column(name = "experience", length = 2000)
    private String experience;              // Ish tajribasi (mas: "3 yil Java da ishlagan")

    @Size(max = 1000, message = "Ta'lim 1000 belgidan oshmasligi kerak")
    @Column(name = "education", length = 1000)
    private String education;               // Ta'lim (mas: "TATU, 2020-2024")

    @Size(max = 1000, message = "Ko'nikmalar 1000 belgidan oshmasligi kerak")
    @Column(name = "skills", length = 1000)
    private String skills;                  // Ko'nikmalar (mas: "Java, Spring, SQL")

    @Size(max = 2000, message = "O'zi haqida 2000 belgidan oshmasligi kerak")
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
