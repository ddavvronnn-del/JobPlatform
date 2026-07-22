package uz.imaan.jobplatform.authModuli.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.imaan.jobplatform.authModuli.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
