package uz.imaan.jobplatform.employer.dto;

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

    // post
    private Long employerChatId;
    private String inn;
    private String passportSeriesNumber;
    private String phoneNumber;

    private String title;            // <-- Qo'shildi (Ish sarlavhasi uchun)
    private String category;
    private String jobType;
    private String salary;           // <-- String ga o'tkazildi (Matn ko'rinishida saqlash uchun)
    private String workHours;        // <-- String ga o'tkazildi ("09:00 - 18:00" uchun)
    private LocalDate jobDate;
    private Integer workerCount;
    private String requirements;
    private Boolean foodProvided;

    private Double latitude;
    private Double longitude;
}