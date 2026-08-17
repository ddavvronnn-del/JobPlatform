package uz.imaan.jobplatform.jobseeker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false) // Bu yerda Telegram ChatId (userId) saqlanadi
    private Long userId;

    @Column(name = "category", nullable = false)
    private String category;
}