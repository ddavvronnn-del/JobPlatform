package uz.imaan.jobplatform.employer.repository;

import uz.imaan.jobplatform.employer.entity.EmployerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployerRepository extends JpaRepository<EmployerEntity, Long> {

    List<EmployerEntity> findByEmployerChatId(Long employerChatId);

    List<EmployerEntity> findByStatus(String status);

    boolean existsByInn(String inn);
}
