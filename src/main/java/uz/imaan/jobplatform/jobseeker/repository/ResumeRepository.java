package uz.imaan.jobplatform.jobseeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.imaan.jobplatform.jobseeker.entity.Resume;

import java.util.List;
import java.util.Optional;


@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // Berilgan ish izlovchining barcha rezyumelari
    List<Resume> findByJobSeekerId(Long jobSeekerId);

    // Berilgan ID va ish izlovchiga tegishli rezyume
    Optional<Resume> findByIdAndJobSeekerId(Long id, Long jobSeekerId);

    // Ish izlovchining aktiv rezyumelari
    List<Resume> findByJobSeekerIdAndIsActiveTrue(Long jobSeekerId);

    // Ish izlovchining eng so'nggi aktiv rezyumesi
    Optional<Resume> findFirstByJobSeekerIdAndIsActiveTrueOrderByCreatedAtDesc(Long jobSeekerId);

    // Ish izlovchining barcha rezyumelarini o'chirish
    void deleteByJobSeekerId(Long jobSeekerId);

}
