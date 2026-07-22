package uz.imaan.jobplatform.authModuli.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {


    @NotBlank(message = "Telefon raqam bo'sh bo'lmasligi kerak")
    private String phone;

    @NotBlank(message = "Parol bo'sh bo'lmasligi kerak")
    private String password;

    private String fullName;
    private String language;
    private String role;
}
