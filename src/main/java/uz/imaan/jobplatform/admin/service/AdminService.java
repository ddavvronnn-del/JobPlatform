package uz.imaan.jobplatform.admin.service;

import uz.imaan.jobplatform.admin.Admin;
import uz.imaan.jobplatform.admin.dto.AdminDto;
import uz.imaan.jobplatform.admin.mapper.AdminMapper;
import uz.imaan.jobplatform.admin.repostory.AdminRepostory;

import java.util.List;
import java.util.stream.Collectors;

public class AdminService {
    private final AdminRepostory.AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    public AdminService(AdminRepostory.AdminRepository adminRepository, AdminMapper adminMapper) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
    }

    public List<AdminDto.AdminDTO> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(adminMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AdminDto.AdminDTO getAdminById(Long id) {
        Admin entity = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
        return adminMapper.toDTO(entity);
    }

    public AdminDto.AdminDTO createAdmin(AdminDto.AdminDTO adminDTO) {
        Admin entity = adminMapper.toEntity(adminDTO);
        Admin savedEntity = adminRepository.save(entity);
        return adminMapper.toDTO(savedEntity);
    }

    public boolean isAdmin(Long telegramId) {
        return adminRepository.existsByTelegramId(telegramId);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}

