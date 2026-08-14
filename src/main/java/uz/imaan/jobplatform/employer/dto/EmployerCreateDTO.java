package uz.imaan.jobplatform.employer.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerCreateDTO {

    @NotNull(message = "Chat ID bo'sh bo'lishi mumkin emas")
    private Long employerChatId;

    @Pattern(regexp = "^[0-9]{9}$", message = "INN 9 ta raqamdan iborat bo'lishi kerak")
    private String inn;

    // Pasport seriyasi: 2 ta KATTA harf va 7 ta raqam (Masalan: AA1234567)
    @NotBlank(message = "Pasport ma'lumotlari kiritilishi shart")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{7}$", message = "Pasport seriyasi faqat katta harf va 7 ta raqamdan iborat bo'lishi kerak (masalan: AA1234567)")
    private String passportSeriesNumber;

    @NotBlank(message = "Telefon raqam bo'sh bo'lmasligi kerak")
    @Pattern(regexp = "^998[0-9]{9}$", message = "Telefon raqam noto'g'ri (masalan: 998901234567)")
    private String phoneNumber;

    @NotBlank(message = "Ish sarlavhasi kiritilishi shart")
    @Size(min = 3, max = 100, message = "Sarlavha 3 va 100 belgilar orasida bo'lishi kerak")
    private String title;

    @NotBlank(message = "Kategoriya tanlanishi shart")
    private String category;

    @NotBlank(message = "Ish turi tanlanishi shart")
    private String jobType;

    @NotBlank(message = "Maosh ko'rsatilishi shart")
    @Pattern(regexp = "^[0-9]+$", message = "Maosh faqat musbat raqamlardan iborat bo'lishi kerak")
    private String salary;

    @Pattern(regexp = "^[0-9]+$", message = "Ish soati faqat raqam bo'lishi kerak")
    private String workHours;

    private LocalDate jobDate;

    @NotNull(message = "Xodimlar soni ko'rsatilishi shart")
    @Positive(message = "Xodimlar soni kamida 1 ta bo'lishi kerak")
    private Integer workerCount;

    @Size(max = 1000, message = "Talablar 1000 ta belgidan oshmasligi kerak")
    private String requirements;

    private Boolean foodProvided;
    private Double latitude;
    private Double longitude;
}