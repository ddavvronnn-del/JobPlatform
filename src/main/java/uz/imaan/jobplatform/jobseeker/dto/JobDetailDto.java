package uz.imaan.jobplatform.jobseeker.dto;



import java.math.BigDecimal;
import java.time.LocalDate;



public record JobDetailDto (
     Long id,
     String title,
     BigDecimal salary,      // To'lovi
     LocalDate date,         // Sana
     String location,       // Manzil
     String requirements,   // Talablar
     Long employerId,
     String employerName,
     Double employerRating // Ish beruvchi reytingi
){}
