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
public class EmployerUpdateDTO {

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
