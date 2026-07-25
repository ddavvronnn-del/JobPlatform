package admin.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import admin.Admin;

import java.util.Optional;

public class AdminRepostory {


    @Repository
    public interface AdminRepository extends JpaRepository<Admin, Long> {
        Optional<Admin> findByTelegramId(Long telegramId);
        boolean existsByTelegramId(Long telegramId);
    }
}
