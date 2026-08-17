package uz.imaan.jobplatform.admin.mapper;

import org.springframework.stereotype.Component;
import uz.imaan.jobplatform.admin.dto.AdminDtoTwo;
import uz.imaan.jobplatform.admin.entity.Admin;

@Component
public class AdminMapper {

    public AdminDtoTwo toDTO(Admin entity) {
        if (entity == null) return null;

        return new AdminDtoTwo(
                entity.getId(),
                entity.getTelegramId(),
                entity.getUsername(),
                entity.getRole() != null ? entity.getRole().toString() : null,
                entity.getIsActive(),
                null, // numberOfUsers
                null, // numberOfRequests
                null, // totalWorkers
                null, // totalEmployers
                0L,   // completedShifts
                null, // activeJobs
                null, // completedJobs
                null, // totalAdmins
                null, // userId
                null, // reason
                null, // email
                null, // password
                null, // totalJobs
                0L,   // totalVacancies
                0L    // activeShifts
        );
    }

    public Admin toEntity(AdminDtoTwo dto) {
        if (dto == null) return null;

        Admin entity = new Admin();
        entity.setId(dto.id());
        entity.setTelegramId(dto.telegramId());
        entity.setUsername(dto.username());
        entity.setIsActive(dto.isActive());
        return entity;
    }
}