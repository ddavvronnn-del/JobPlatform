package uz.imaan.jobplatform.jobseeker.service.interfaces;



import uz.imaan.jobplatform.jobseeker.dto.BankCardRequest;
import uz.imaan.jobplatform.jobseeker.entity.BankCard;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    // ============================================
    // KARTA SERVICES
    // ============================================
    BankCard addBankCard(Long userId, BankCardRequest request);

    List<BankCard> getMyCards(Long userId);

    void validateExpireDate(String expireDate);

    boolean isCardExpired(String expireDate);

    // ============================================
    // TRANZAKSIYA SERVICES
    // ============================================
    List<WalletTransaction> getTransactionHistory(Long userId);

    String withdrawMoney(Long userId, BigDecimal amount);

    // ============================================
    // PROFIL SERVICES (Wallet uchun)
    // ============================================
    JobSeekerProfile getProfileByUserId(Long userId);

}