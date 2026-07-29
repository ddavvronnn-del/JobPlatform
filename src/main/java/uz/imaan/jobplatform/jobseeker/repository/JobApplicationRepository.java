package uz.imaan.jobplatform.jobseeker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;



import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobSeekerId(Long jobSeekerId);

    Page<JobApplication> findByJobSeekerId(Long jobSeekerId, Pageable pageable);


    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    List<JobApplication> findByJobSeekerIdAndStatus(Long jobSeekerId, JobApplication.ApplicationStatus status);

    Optional<JobApplication> findByIdAndJobSeekerId(Long id, Long jobSeekerId);

}
