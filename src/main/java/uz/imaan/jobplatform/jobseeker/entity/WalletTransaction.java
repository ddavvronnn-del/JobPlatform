package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ish izlovchi ID si kiritilishi shart")
    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;

    @NotNull(message = "Miqdor kiritilishi shart")
    @Positive(message = "Miqdor musbat bo'lishi kerak")
    private BigDecimal amount;

    @NotNull(message = "Tranzaksiya turi kiritilishi shart")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAW
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
