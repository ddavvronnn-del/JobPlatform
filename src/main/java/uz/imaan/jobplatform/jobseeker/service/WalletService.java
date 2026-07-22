package uz.imaan.jobplatform.jobseeker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.imaan.jobplatform.jobseeker.dto.BankCardRequest;
import uz.imaan.jobplatform.jobseeker.entity.BankCard;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import uz.imaan.jobplatform.jobseeker.entity.WalletTransaction;
import uz.imaan.jobplatform.jobseeker.repository.BankCardRepository;
import uz.imaan.jobplatform.jobseeker.repository.JobSeekerProfileRepository;
import uz.imaan.jobplatform.jobseeker.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;






@Service
@RequiredArgsConstructor
public class WalletService {

    private final JobSeekerProfileRepository profileRepository;
    private final BankCardRepository bankCardRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public BankCard addBankCard(Long userId, BankCardRequest request) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        BankCard bankCard = BankCard.builder()
                .cardNumber(request.getCardNumber())
                .expireDate(request.getExpireDate())
                .jobSeeker(profile)
                .build();

        return bankCardRepository.save(bankCard);
    }

    @Transactional(readOnly = true)
    public List<BankCard> getMyCards(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return bankCardRepository.findByJobSeekerId(profile.getId());
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> getTransactionHistory(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return transactionRepository.findByJobSeekerIdOrderByCreatedAtDesc(profile.getId());
    }

    @Transactional
    public String withdrawMoney(Long userId, BigDecimal amount) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        if (profile.getWalletBalance() == null || profile.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Hisobda yetarli mablag' mavjud emas");
        }

        profile.setWalletBalance(profile.getWalletBalance().subtract(amount));
        profileRepository.save(profile);

        WalletTransaction transaction = WalletTransaction.builder()
                .jobSeekerId(profile.getId())
                .amount(amount)
                .type(WalletTransaction.TransactionType.WITHDRAW)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        return "Pul yechish so'rovi muvaffaqiyatli amalga oshirildi!";
    }
}
