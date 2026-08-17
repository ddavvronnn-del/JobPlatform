package uz.imaan.jobplatform.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.admin.TelegramBotService;
import uz.imaan.jobplatform.admin.mapper.AdminMapper;
import uz.imaan.jobplatform.admin.repository.AdminRepository;
import uz.imaan.jobplatform.employer.repository.EmployerRepository;
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
        return (adminIds != null && adminIds.contains(chatId))
                || super.isAdmin(chatId);
    }
}