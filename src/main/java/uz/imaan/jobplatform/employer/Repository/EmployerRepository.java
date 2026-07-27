package uz.imaan.jobplatform.employer.Repository;

import uz.imaan.jobplatform.employer.EmployerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployerRepository extends JpaRepository<EmployerEntity, Long> {

    List<EmployerEntity> findByEmployerChatId(Long employerChatId);

    List<EmployerEntity> findByStatus(String status);
}
