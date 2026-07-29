package uz.imaan.jobplatform.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admins")
@Getter
@Setter
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;
    private String username;
    private String role; // e.g. "ROLE_ADMIN", "SUPER_ADMIN"
    private Boolean isActive;
}