package uz.imaan.jobplatform.admin.repository;



import com.sun.research.ws.wadl.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JobApplicationRepositoryForAdmin extends JpaRepository<Application, Long> {
}
