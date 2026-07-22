package uz.imaan.jobplatform.jobseeker.service;



import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.imaan.jobplatform.jobseeker.dto.ApplyJobRequest;
import uz.imaan.jobplatform.jobseeker.dto.JobSeekerProfileDto;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.mapper.JobSeekerMapper;
import uz.imaan.jobplatform.jobseeker.repository.JobApplicationRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private final JobSeekerProfileRepository profileRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobSeekerMapper mapper;

    @Transactional(readOnly = true)
    public JobSeekerProfileDto getProfileByUserId(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return mapper.toDto(profile);
    }

    @Transactional
    public JobApplication applyForJob(Long userId, ApplyJobRequest request) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        if (applicationRepository.existsByJobIdAndJobSeekerId(request.getJobId(), profile.getId())) {
            throw new IllegalStateException("Siz ushbu ishga allaqachon ariza yuborgansiz!");
        }

        JobApplication application = JobApplication.builder()
                .jobId(request.getJobId())
                .jobSeekerId(profile.getId())
                .coverLetter(request.getCoverLetter())
                .status(JobApplication.ApplicationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getMyApplications(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return applicationRepository.findByJobSeekerId(profile.getId());
    }

}
