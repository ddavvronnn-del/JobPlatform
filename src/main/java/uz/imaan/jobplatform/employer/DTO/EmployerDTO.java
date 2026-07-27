package uz.imaan.jobplatform.employer.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployerDTO {

    private Long id;
    private Long employerChatId;
    private String category;
    private String jobType;
    private Double salary;
    private Integer workHours;
    private LocalDate jobDate;
    private Integer workerCount;
    private String requirements;
    private Boolean foodProvided;
    private Double latitude;
    private Double longitude;
    private String status;
}
