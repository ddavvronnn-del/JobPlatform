package uz.imaan.jobplatform.employer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerResponseDTO {

    private Long id;
    private Long employerChatId;

    private String inn;
    private String passportSeriesNumber;
    private String fullName;
    private String phoneNumber;

    private String category;
    private String jobType;
    private String salary;
    private String workHours;
    private LocalDate jobDate;
    private Integer workerCount;
    private String requirements;
    private Boolean foodProvided;

    private Double latitude;
    private Double longitude;
    private String status;
    private LocalDateTime createdAt;
}
