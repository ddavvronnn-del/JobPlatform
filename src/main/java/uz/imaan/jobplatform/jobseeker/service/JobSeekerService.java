package uz.imaan.jobplatform.jobseeker.service;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.imaan.jobplatform.jobseeker.dto.UpdateProfileRequest;
import uz.imaan.jobplatform.jobseeker.dto.ApplyJobRequest;
import uz.imaan.jobplatform.jobseeker.dto.JobApplicationDto;
import uz.imaan.jobplatform.jobseeker.dto.JobSeekerProfileDto;
import uz.imaan.jobplatform.jobseeker.dto.SettingsUpdateRequest;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.mapper.JobSeekerMapper;
import uz.imaan.jobplatform.jobseeker.repository.JobApplicationRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class JobSeekerService {

    private final JobSeekerProfileRepository profileRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobSeekerMapper mapper;

    // --- PROFIL SERVICES ---

    @Transactional(readOnly = true)
    public JobSeekerProfileDto getProfileByUserId(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return mapper.toDto(profile);
    }

    @Transactional
    public JobSeekerProfileDto updateProfile(Long userId, @Valid UpdateProfileRequest dto) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        mapper.updateEntityFromDto(dto, profile);
        JobSeekerProfile updatedProfile = profileRepository.save(profile);
        return mapper.toDto(updatedProfile);
    }

    // --- JOB APPLICATION SERVICES ---

    @Transactional
    public JobApplication applyForJob(Long userId, ApplyJobRequest request) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        // Arizani tekshirish
        if (applicationRepository.existsByJobIdAndJobSeekerId(request.getJobId(), profile.getId())) {
            throw new IllegalStateException("Siz ushbu ishga allaqachon ariza yuborgansiz!");
        }

        JobApplication application = JobApplication.builder()
                .jobId(request.getJobId())
                .jobSeekerId(profile.getId())
                .coverLetter(request.getComment())
                .status(JobApplication.ApplicationStatus.PENDING)
                .build();

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public Page<JobApplication> getMyApplicationsWithPagination(Long userId, Pageable pageable) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return applicationRepository.findByJobSeekerId(profile.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationDto> getMyApplications(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        return applicationRepository.findByJobSeekerId(profile.getId())
                .stream()
                .map(application -> JobApplicationDto.builder()
                        .id(application.getId())
                        .jobId(application.getJobId())
                        .jobSeekerId(application.getJobSeekerId())
                        .status(application.getStatus().name())
                        .coverLetter(application.getCoverLetter())
                        .appliedAt(application.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getMyActiveJobs(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        return applicationRepository.findByJobSeekerIdAndStatus(
                profile.getId(),
                JobApplication.ApplicationStatus.ACCEPTED
        );
    }

    @Transactional
    public void cancelApplication(Long userId, Long applicationId) {
        // 1. Profilni topish
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        // 2. Arizani topish va tekshirish
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Ariza topilmadi: " + applicationId));

        // 3. Arizaning ushbu foydalanuvchiga tegishli ekanligini tekshirish
        if (!application.getJobSeekerId().equals(profile.getId())) {
            throw new SecurityException("Bu arizani bekor qilish huquqi yo'q!");
        }

        // 4. Arizaning holatini tekshirish
        if (application.getStatus() == JobApplication.ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException("Qabul qilingan arizani bekor qilib bo'lmaydi!");
        }

        if (application.getStatus() == JobApplication.ApplicationStatus.CANCELLED) {
            throw new IllegalStateException("Bu ariza allaqachon bekor qilingan!");
        }

        // 5. Arizani bekor qilish
        application.setStatus(JobApplication.ApplicationStatus.CANCELLED);
        applicationRepository.save(application);

        log.info("Ariza bekor qilindi: applicationId={}, userId={}", applicationId, userId);
    }

    // --- SETTINGS SERVICES ---

    @Transactional
    public void updateSettings(Long userId, SettingsUpdateRequest request) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        if (request.getLanguage() != null) {
            profile.setLanguage(request.getLanguage());
        }
        // Boshqa sozlamalarni yangilash
        // profile.setNotificationsEnabled(request.getNotificationsEnabled());
        // profile.setProfileHidden(request.getProfileHidden());

        profileRepository.save(profile);
        log.info("Sozlamalar yangilandi: userId={}", userId);
    }

    // --- ADDITIONAL METHODS ---

    public JobSeekerProfile getProfileEntity(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
    }


    public SendMessage handleJobSeeker(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText("👋 JobSeeker menyusi!\n\n" +
                "📌 Komandalar:\n" +
                "/profile - Profilim\n" +
                "/jobs - Ish qidirish\n" +
                "/wallet - Hamyon\n" +
                "/applications - Arizalar\n" +
                "/settings - Sozlamalar");

        return sendMessage;
    }
}
