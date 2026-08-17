package uz.imaan.jobplatform.jobseeker.dto;


import java.math.BigDecimal;



public record JobSeekerProfileDto (

     Long id,
     Long userId,
     String fullName,
     String phoneNumber,
     String profession,
     Double rating,
     String language,
     String portfolio,
     BigDecimal walletBalance,
     String passportNumber

){}
