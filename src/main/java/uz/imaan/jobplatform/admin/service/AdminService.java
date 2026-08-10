package uz.imaan.jobplatform.admin.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.admin.TelegramBotService;
import uz.imaan.jobplatform.admin.entity.Admin;

import uz.imaan.jobplatform.admin.dto.AdminDTO;
import uz.imaan.jobplatform.admin.mapper.AdminMapper;
import uz.imaan.jobplatform.admin.repository.AdminRepository;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;


import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    @Lazy
    @Autowired
    private  TelegramBotService telegramBotService;


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
                "👤 **Профиль исполнителя** (ID: %d)\n" +
                        "📛 **ФИО:** %s\n" +
                        "📞 **Телефон:** %s\n\n" +
                        "📌 **Статус:** %s\n" +
                        "🔒 **Заблокирован:** %s\n" +
                        "💬 **Причина блокировки:** %s\n\n" +
                        "📊 **Характеристики:**\n" +
                        "• Профессия: %s\n" +
                        "• Опыт: %s\n" +
                        "• Рейтинг: %.1f ⭐",
                userId,                                                                      // 1. %d
                worker.getFullName() != null ? worker.getFullName() : "Не указано",           // 2. %s
                worker.getPhoneNumber() != null ? worker.getPhoneNumber() : "Не указан",       // 3. %s
                Boolean.TRUE.equals(worker.getIsActive()) ? "Активен ✅" : "Заблокирован ❌", // 4. %s
                Boolean.TRUE.equals(worker.getIsActive()) ? "Нет" : "Да",                    // 5. %s
                worker.getBlockReason() != null ? worker.getBlockReason() : "Нет",           // 6. %s
                worker.getProfession() != null ? worker.getProfession() : "Не указана",     // 7. %s
                worker.getExperience() != null ? worker.getExperience() : "Не указан",       // 8. %s
                worker.getRating() != null ? worker.getRating() : 0.0                       // 9. %.1f
        );
    }

    // Этот метод запустится один раз при старте приложения
    @jakarta.annotation.PostConstruct
    public void init() {
        long myRealId = 6326035618L ; // ВАШ ID ИЗ ЛОГОВ

        // Проверяем, есть ли вы в базе
        if (!adminRepository.existsByTelegramId(myRealId)) {
            // Если нет - создаем вас принудительно
            Admin newAdmin = new Admin();
            newAdmin.setTelegramId(myRealId);
            newAdmin.setName("Главный Админ");
            newAdmin.setRole("ADMIN");

            adminRepository.save(newAdmin);
            System.out.println("✅ АДМИН УСПЕШНО ДОБАВЛЕН В БАЗУ ДАННЫХ!");
        } else {
            System.out.println("ℹ️ Админ уже есть в базе.");
        }
    }

    private List<Long> adminChatIds = List.of(
            6326035618L,
            7584728450L,
            6500351879L
    );

    public void notifyAllAdmins(String message) {
        for (Long chatId : adminChatIds) {
            telegramBotService.sendMessage(chatId, message);
        }
    }

    
}


