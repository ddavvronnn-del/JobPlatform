package uz.imaan.jobplatform.admin.service;

import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.admin.Admin;

import uz.imaan.jobplatform.admin.dto.AdminDTO;
import uz.imaan.jobplatform.admin.mapper.AdminMapper;
import uz.imaan.jobplatform.admin.repostory.AdminRepository;


import java.util.List;
import java.util.stream.Collectors;
@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    public AdminService(AdminRepository adminRepository, AdminMapper adminMapper) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
    }

    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(adminMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AdminDTO getAdminById(Long id) {
        Admin entity = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
        return adminMapper.toDTO(entity);
    }

    public AdminDTO createAdmin(AdminDTO adminDTO) {
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

