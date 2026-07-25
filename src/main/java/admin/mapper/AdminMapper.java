package admin.mapper;


import org.springframework.stereotype.Component;
import admin.Admin;
import admin.dto.AdminDto;

@Component
public class AdminMapper {

    public AdminDto.AdminDTO toDTO(Admin entity) {
        if (entity == null) return null;

        AdminDto.AdminDTO dto = new AdminDto.AdminDTO();
        dto.setId(entity.getId());
        dto.setTelegramId(entity.getTelegramId());
        dto.setUsername(entity.getUsername());
        dto.setRole(entity.getRole());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    public Admin toEntity(AdminDto.AdminDTO dto) {
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
