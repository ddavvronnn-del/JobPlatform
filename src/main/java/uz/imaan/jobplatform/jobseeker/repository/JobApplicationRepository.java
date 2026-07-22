package uz.imaan.jobplatform.jobseeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobSeekerId(Long jobSeekerId);
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

}
