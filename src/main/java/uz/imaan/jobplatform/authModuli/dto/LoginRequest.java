package uz.imaan.jobplatform.authModuli.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class LoginRequest {
    @NotBlank(message = "Telefon raqam kiritilishi shart")
    private String phone;

    @NotBlank(message = "Parol kiritilishi shart")
    private String password;
}
