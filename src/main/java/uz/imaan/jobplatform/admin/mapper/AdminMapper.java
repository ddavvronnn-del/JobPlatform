package uz.imaan.jobplatform.admin.mapper;


import org.springframework.stereotype.Component;
import uz.imaan.jobplatform.admin.Admin;
import uz.imaan.jobplatform.admin.dto.AdminDTO;


@Component
public class AdminMapper {

    public AdminDTO toDTO(Admin entity) {
        if (entity == null) return null;

        AdminDTO dto = new AdminDTO();
        dto.setId(entity.getId());
        dto.setTelegramId(entity.getTelegramId());
        dto.setUsername(entity.getUsername());
        dto.setRole(entity.getRole());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    public Admin toEntity(AdminDTO dto) {
        if (dto == null) return null;

        Admin entity = new Admin();
        entity.setId(dto.getId());
        entity.setTelegramId(dto.getTelegramId());
        entity.setUsername(dto.getUsername());
        entity.setRole(dto.getRole());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }
}
