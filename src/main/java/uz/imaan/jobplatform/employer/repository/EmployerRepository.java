package uz.imaan.jobplatform.employer.repository;

import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<EmployerEntity, Long> {

    List<EmployerEntity> findByStatus(String status);

    Optional<EmployerEntity> findByEmployerChatId(Long employerChatId);

    boolean existsByInn(String inn);
}
