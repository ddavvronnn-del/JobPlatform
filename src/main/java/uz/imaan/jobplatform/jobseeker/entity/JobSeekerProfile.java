package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "job_seekers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JobSeekerProfile {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String profession;
    private Double rating;
    private String language;
    private String passportNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(length = 1000)
    private String portfolio;

    @Column(name = "wallet_balance", precision = 19, scale = 2)
    private BigDecimal walletBalance;

    // ============================================
    // YANGI FIELD'LAR
    // ============================================
    @Column(name = "experience", length = 1000)
    private String experience;

    @Column(name = "preferred_job_type")
    private String preferredJobType;

    @Column(name = "category")
    private String category;

    // ============================================
    // ADMIN UCHUN
    // ============================================
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "block_reason")
    private String blockReason;

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BankCard> bankCards = new ArrayList<>();
}
