package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class UpdateProfileRequest {

    @NotBlank(message = "Ism-familiya bo'sh bo'lishi mumkin emas")
    private String fullName;
    private String phoneNumber;
    private String profession;
    private String language;
    private String portfolio;
    private String passportNumber;



}
