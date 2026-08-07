package uz.imaan.jobplatform.admin.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.imaan.jobplatform.application.entity.ApplicationEntity;


@Repository
public interface JobApplicationRepositoryForAdmin extends JpaRepository<ApplicationEntity, Long> {
}
