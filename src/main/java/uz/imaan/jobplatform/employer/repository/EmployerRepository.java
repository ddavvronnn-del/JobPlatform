package uz.imaan.jobplatform.employer.repository;

import feign.Param;
import org.springframework.data.jpa.repository.Query;
import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<EmployerEntity, Long> {
    @Query("SELECT e FROM Employer e WHERE e.userId = :tgId OR e.id = :tgId")
    Optional<EmployerEntity> findByTgId(@Param("tgId") Long tgId);
    List<EmployerEntity> findByStatus(String status);

    Optional<EmployerEntity> findByEmployerChatId(Long employerChatId);

    boolean existsByInn(String inn);
}
