package uz.imaan.jobplatform.employer.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record EmployerResponseDTO(
        Long id,
        Long employerChatId,
        String inn,
        String passportSeriesNumber,
        String fullName,
        String phoneNumber,
        String title,          // 📌 Sarlavha qo'shildi
        String category,
        String jobType,
        String salary,
        String workHours,
        LocalDate jobDate,
        Integer workerCount,
        String requirements,
        Boolean foodProvided,
        Double latitude,
        Double longitude,
        String status,
        LocalDateTime createdAt
) {}