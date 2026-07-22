package uz.imaan.jobplatform.jobseeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.imaan.jobplatform.jobseeker.entity.WalletTransaction;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByJobSeekerIdOrderByCreatedAtDesc(Long jobSeekerId);

}
