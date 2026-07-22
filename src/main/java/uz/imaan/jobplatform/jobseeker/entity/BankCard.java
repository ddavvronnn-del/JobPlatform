package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "bank_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "expire_date")
    private String expireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id")
    private JobSeekerProfile jobSeeker;

}
