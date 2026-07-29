package uz.imaan.jobplatform.jobseeker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class JobDetailDto {
    private Long id;
    private String title;
    private BigDecimal salary;      // To'lovi
    private LocalDate date;         // Sana
    private String location;       // Manzil
    private String requirements;   // Talablar
    private Long employerId;
    private String employerName;
    private Double employerRating; // Ish beruvchi reytingi
}
