package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;





public record UpdateProfileRequest (

    @NotBlank(message = "Ism-familiya bo'sh bo'lishi mumkin emas")
     String fullName,
     String phoneNumber,
     String profession,
     String language,
     String portfolio,
     String passportNumber

){}