package uz.imaan.jobplatform.jobseeker.service;



import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        // Karta raqamini tekshirish (oddiy validatsiya)
        if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
            throw new IllegalArgumentException("Noto'g'ri karta raqami!");
        }

        BankCard bankCard = BankCard.builder()
                .cardNumber(request.getCardNumber())
                .expireDate(request.getExpireDate())
                .jobSeeker(profile)
                .build();

        BankCard savedCard = bankCardRepository.save(bankCard);
        log.info("Yangi karta qo'shildi: userId={}, cardId={}", userId, savedCard.getId());
        return savedCard;
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
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Miqdor 0 dan katta bo'lishi kerak!");
        }

        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        // Kartalar mavjudligini tekshirish
        List<BankCard> cards = bankCardRepository.findByJobSeekerId(profile.getId());
        if (cards.isEmpty()) {
            throw new IllegalStateException("Pul yechish uchun avval karta qo'shing!");
        }

        if (profile.getWalletBalance() == null || profile.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Hisobda yetarli mablag' mavjud emas! Balans: " + profile.getWalletBalance());
        }

        // Pulni yechish
        profile.setWalletBalance(profile.getWalletBalance().subtract(amount));
        profileRepository.save(profile);

        // Tranzaksiyani saqlash
        WalletTransaction transaction = WalletTransaction.builder()
                .jobSeekerId(profile.getId())
                .amount(amount)
                .type(WalletTransaction.TransactionType.WITHDRAW)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("Pul yechildi: userId={}, amount={}", userId, amount);

        return "Pul yechish so'rovi muvaffaqiyatli amalga oshirildi! Yechilgan summa: " + amount;
    }
}
