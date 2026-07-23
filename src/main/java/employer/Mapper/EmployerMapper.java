package employer.Mapper;

import employer.DTO.EmployerDTO;
import employer.EmployerEntity;
import org.springframework.stereotype.Component;

@Component
public class EmployerMapper {
    public EmployerDTO toDTO(EmployerEntity entity) {
        if (entity == null) return null;
        return EmployerDTO.builder()
                .id(entity.getId())
                .employerChatId(entity.getEmployerChatId())
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
                .build();
    }

    public EmployerEntity toEntity(EmployerDTO dto) {
        if (dto == null) return null;
        return EmployerEntity.builder()
                .id(dto.getId())
                .employerChatId(dto.getEmployerChatId())
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
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .build();
    }
}
