package uz.imaan.jobplatform.employer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record EmployerUpdateDTO(
        String category,
        String jobType,
        @NotBlank
        Double salary,
        @Positive
        Integer workHours,
        LocalDate jobDate,
        Integer workerCount,
        String requirements,
        Boolean foodProvided,
        Double latitude,
        Double longitude,
        String status
) {}