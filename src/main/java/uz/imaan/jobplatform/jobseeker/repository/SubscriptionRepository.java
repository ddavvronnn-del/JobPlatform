package uz.imaan.jobplatform.jobseeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.imaan.jobplatform.jobseeker.entity.Subscription;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    // Kategoriya bo'yicha barcha obunachilarni olish
    List<Subscription> findByCategory(String category);

    // Ishchi allaqachon bu kategoriyaga obuna bo'lganmi?
    boolean existsByUserIdAndCategory(Long userId, String category);
}