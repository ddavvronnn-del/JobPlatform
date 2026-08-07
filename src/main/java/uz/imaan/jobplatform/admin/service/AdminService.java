package uz.imaan.jobplatform.admin.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.admin.entity.Admin;

import uz.imaan.jobplatform.admin.dto.AdminDTO;
import uz.imaan.jobplatform.admin.mapper.AdminMapper;
import uz.imaan.jobplatform.admin.repository.AdminRepository;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;


import java.util.List;
import java.util.stream.Collectors;



@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;

    public AdminService(AdminRepository adminRepository, AdminMapper adminMapper, JobSeekerProfileRepository jobSeekerProfileRepository) {
        this.adminRepository = adminRepository;
        this.adminMapper = adminMapper;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
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

    public AdminDTO getStats() {
        return AdminDTO.builder()
                .totalAdmins(adminRepository.count())
                // Заглушки или реальные вызовы из твоих репозиториев:
                .totalEmployers(0L) // employerRepository.count()
                .totalWorkers(0L)   // workerRepository.count()
                .totalJobs(0L)      // jobRepository.count()
                .activeJobs(0L)     // jobRepository.countByStatus("ACTIVE")
                .completedJobs(0L)  // jobRepository.countByStatus("COMPLETED")
                .build();
    }

    @Transactional
    public void blockUser(@NotNull AdminDTO blockDTO) {
        // 1. Используем переменную репозитория с маленькой буквы
        JobSeekerProfile worker = jobSeekerProfileRepository.findByUserId(blockDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + blockDTO.getUserId()));

        worker.setIsActive(false);
        worker.setBlockReason(blockDTO.getReason());

        // 2. Сохраняем через экземпляр репозитория (с маленькой буквы)
        jobSeekerProfileRepository.save(worker);

        System.out.println("Пользователь с ID " + blockDTO.getUserId() + " заблокирован по причине: " + blockDTO.getReason());
    }

    @Transactional
    public void unblockUser(Long userId) {
        JobSeekerProfile worker = jobSeekerProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        worker.setIsActive(true);
        worker.setBlockReason(null);

        jobSeekerProfileRepository.save(worker);

        System.out.println("Пользователь с ID " + userId + " успешно разблокирован.");
    }

    @Transactional()
    public String getWorkerDetails(Long userId) {
        JobSeekerProfile worker = jobSeekerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Исполнитель не найден с ID: " + userId));

        return String.format(
                "👤 **Профиль исполнителя** (ID: %d)\n\n" +
                        "📌 **Статус:** %s\n" +
                        "🔒 **Заблокирован:** %s\n" +
                        "💬 **Причина блокировки:** %s\n\n" +
                        "📊 **Характеристики:**\n" +
                        "• Навыки / Опыт: %s\n" +
                        "• Рейтинг: %.1f ⭐\n" +
                        "• Выполнено смен: %d",
                userId,
                worker.getIsActive() ? "Активен ✅" : "Заблокирован ❌",
                worker.getIsActive() ? "Нет" : "Да",
                worker.getBlockReason() != null ? worker.getBlockReason() : "Нет",
                worker.getRating() != null ? worker.getRating() : 0.0
        );
    }


    // Метод для отправки уведомления админу из любых других сервисов


}


