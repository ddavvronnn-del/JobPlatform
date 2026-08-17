package uz.imaan.jobplatform.jobseeker.dto;



import java.time.LocalDateTime;



public record ResumeDto (

     Long id,
     String title,
     String fullName,
     String email,
     String phoneNumber,
     String profession,
     String experience,
     String education,
     String skills,
     String about,
     Boolean isActive,
     LocalDateTime createdAt,
     LocalDateTime updateAt

){}