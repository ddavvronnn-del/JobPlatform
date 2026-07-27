package uz.imaan.jobplatform.jobseeker.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uz.imaan.jobplatform.jobseeker.dto.JobSeekerProfileDto;


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



}
