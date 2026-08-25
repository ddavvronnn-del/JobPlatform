/*
package uz.imaan.jobplatform.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.admin.TelegramBotService;
import uz.imaan.jobplatform.admin.mapper.AdminMapper;
import uz.imaan.jobplatform.admin.repository.AdminRepository;
import uz.imaan.jobplatform.employer.entity.Employer;
import uz.imaan.jobplatform.employer.repository.EmployerRepository;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;

import java.util.List;

@Service
public class AdminServiceImpl extends AdminService {

    @Value("${telegram.bot.admin-ids:}")
    private List<Long> adminIds;

    public AdminServiceImpl(AdminRepository adminRepository,
                            AdminMapper adminMapper,
                            JobSeekerProfileRepository jobSeekerProfileRepository,
                            EmployerRepository employerRepository,
                            @Lazy TelegramBotService telegramBotService) {
        super(adminRepository, adminMapper, jobSeekerProfileRepository, employerRepository, telegramBotService);
    }

    @Override
    public boolean isAdmin(Long chatId) {
        if (chatId == null) return false;
        return (adminIds != null && adminIds.contains(chatId)) || super.isAdmin(chatId);
    }

    public String getFormattedJobSeekersList() {
        List<JobSeekerProfile> list = jobSeekerProfileRepository.findAll();
        if (list.isEmpty()) return "👷 Рабочие не найдены.";

        StringBuilder sb = new StringBuilder("👷 *Список всех рабочих (соискателей):*\n\n");
        int count = 1;
        for (JobSeekerProfile profile : list) {
            Long tgId = profile.getUserId() != null ? profile.getUserId() : profile.getId();
            sb.append(count++).append(". ")
                    .append(profile.getFullName() != null ? profile.getFullName() : "Без имени")
                    .append(" | 🆔 `").append(tgId).append("`")
                    .append(" | 📞 ").append(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "—")
                    .append("\n");
        }
        return sb.toString();
    }

    public String getFormattedEmployersList() {
        List<Employer> list = employerRepository.findAll();
        if (list.isEmpty()) return "🏢 Работодатели не найдены.";

        StringBuilder sb = new StringBuilder("🏢 *Список всех работодателей:*\n\n");
        int count = 1;
        for (Employer emp : list) {
            Long tgId = emp.getUserId() != null ? emp.getUserId() : emp.getId();
            sb.append(count++).append(". ")
                    .append("Yosh: ").append(emp.getAge()).append(", Pasport: ").append(emp.getPassport() != null ? emp.getPassport() : "—")
                    .append(" | 🆔 `").append(tgId).append("`")
                    .append(" | 📞 ").append(emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "—")
                    .append("\n");
        }
        return sb.toString();
    }

    public String getUserInfoByTgId(Long targetTgId) {
        if (targetTgId == null) return "⚠️ Введите корректный ID.";

        JobSeekerProfile worker = jobSeekerProfileRepository.findByTgId(targetTgId).orElse(null);
        Employer employer = employerRepository.findByTgId(targetTgId).orElse(null);

        if (worker == null && employer == null) {
            return "❌ Пользователь с ID `" + targetTgId + "` не найден в базе данных.";
        }

        StringBuilder info = new StringBuilder("👤 *Информация о пользователе*\n");
        info.append("───────────────────────────\n");
        info.append("🆔 *ID:* `").append(targetTgId).append("`\n\n");

        if (worker != null) {
            info.append("👷 *Роль:* Соискатель (Рабочий)\n");
            info.append("📛 *Имя:* ").append(worker.getFullName() != null ? worker.getFullName() : "Не указано").append("\n");
            info.append("📞 *Телефон:* ").append(worker.getPhoneNumber() != null ? worker.getPhoneNumber() : "Не указан").append("\n\n");
        }

        if (employer != null) {
            info.append("🏢 *Роль:* Работодатель\n");
            info.append("📄 *Паспорт:* ").append(employer.getPassport() != null ? employer.getPassport() : "Не указан").append("\n");
            info.append("🎂 *Возраст:* ").append(employer.getAge()).append("\n");
            info.append("📞 *Телефон:* ").append(employer.getPhoneNumber() != null ? employer.getPhoneNumber() : "Не указан").append("\n");
        }

        return info.toString();
    }
}*/
