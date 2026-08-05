package uz.imaan.jobplatform.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.imaan.jobplatform.application.entity.ApplicationEntity;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

}