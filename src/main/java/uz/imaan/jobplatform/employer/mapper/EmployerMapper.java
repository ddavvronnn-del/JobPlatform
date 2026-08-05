package uz.imaan.jobplatform.employer.mapper;

import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import org.springframework.stereotype.Component;
import uz.imaan.jobplatform.employer.dto.EmployerCreateDTO;
import uz.imaan.jobplatform.employer.dto.EmployerResponseDTO;

@Component
public class EmployerMapper {
    public EmployerEntity toEntity(EmployerCreateDTO dto) {
        if (dto == null) return null;

        return EmployerEntity.builder()
                .employerChatId(dto.getEmployerChatId())
                .inn(dto.getInn())
                .passportSeriesNumber(dto.getPassportSeriesNumber())
                .phoneNumber(dto.getPhoneNumber())
                .category(dto.getCategory())
                .jobType(dto.getJobType())
                .salary(dto.getSalary())
                .workHours(dto.getWorkHours())
                .jobDate(dto.getJobDate())
                .workerCount(dto.getWorkerCount())
                .requirements(dto.getRequirements())
                .foodProvided(dto.getFoodProvided())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    public EmployerResponseDTO toResponseDTO(EmployerEntity entity) {
        if (entity == null) return null;

        return EmployerResponseDTO.builder()
                .id(entity.getId())
                .employerChatId(entity.getEmployerChatId())
                .inn(entity.getInn())
                .passportSeriesNumber(entity.getPassportSeriesNumber())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .category(entity.getCategory())
                .jobType(entity.getJobType())
                .salary(entity.getSalary())
                .workHours(entity.getWorkHours())
                .jobDate(entity.getJobDate())
                .workerCount(entity.getWorkerCount())
                .requirements(entity.getRequirements())
                .foodProvided(entity.getFoodProvided())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
