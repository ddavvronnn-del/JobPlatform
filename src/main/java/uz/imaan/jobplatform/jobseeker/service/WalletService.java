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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final JobSeekerProfileRepository profileRepository;
    private final BankCardRepository bankCardRepository;
    private final WalletTransactionRepository transactionRepository;

    // ============================================
    // KARTA QO'SHISH
    // ============================================
    @Transactional
    public BankCard addBankCard(Long userId, BankCardRequest request) {
        log.info("📝 Karta qo'shish boshlandi: userId={}, cardNumber={}", userId, request.getCardNumber());

        // 1. Profilni topish
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("❌ Profil topilmadi: userId={}", userId);
                    return new RuntimeException("Ishchi profili topilmadi: " + userId);
                });

        log.info("✅ Profil topildi: id={}, fullName={}", profile.getId(), profile.getFullName());

        // 2. Karta raqamini tekshirish (16 ta raqam)
        if (request.getCardNumber() == null || request.getCardNumber().length() != 16) {
            log.error("❌ Karta raqami 16 xona emas: {}", request.getCardNumber());
            throw new IllegalArgumentException("Karta raqami 16 ta raqamdan iborat bo'lishi kerak!");
        }

        // 3. Karta raqamini faqat raqamlardan iboratligini tekshirish
        if (!request.getCardNumber().matches("\\d+")) {
            log.error("❌ Karta raqami faqat raqamlardan iborat emas: {}", request.getCardNumber());
            throw new IllegalArgumentException("Karta raqami faqat raqamlardan iborat bo'lishi kerak!");
        }

        // 4. Karta raqami oldin ro'yxatdan o'tmaganligini tekshirish
        if (bankCardRepository.existsByCardNumber(request.getCardNumber())) {
            log.error("❌ Karta allaqachon ro'yxatdan o'tgan: {}", request.getCardNumber());
            throw new RuntimeException("Bu karta allaqachon ro'yxatdan o'tgan!");
        }

        // 5. Karta muddatini tekshirish (✅ TO'G'RILANDI)
        validateExpireDate(request.getExpireDate());  // ✅ expireDate yuboriladi!

        // 6. Kartani saqlash
        BankCard bankCard = BankCard.builder()
                .cardNumber(request.getCardNumber())
                .expireDate(request.getExpireDate())
                .cardHolderName(request.getCardHolderName().toUpperCase())
                .jobSeeker(profile)
                .isActive(true)
                .build();

        BankCard savedCard = bankCardRepository.save(bankCard);
        log.info("✅ Karta muvaffaqiyatli qo'shildi: id={}, cardNumber={}", savedCard.getId(), savedCard.getCardNumber());

        return savedCard;
    }

    // ============================================
    // KARTA MUDDATINI TEKSHIRISH
    // ============================================
    public void validateExpireDate(String expireDate) {
        log.info("📅 Karta muddatini tekshirish: {}", expireDate);

        if (expireDate == null || expireDate.isEmpty()) {
            throw new IllegalArgumentException("Amal qilish muddati kiritilishi shart!");
        }

        // Format: MM/YY (masalan: 12/26)
        if (!expireDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            throw new IllegalArgumentException("Noto'g'ri format! Format: MM/YY (masalan: 12/26)");
        }

        try {
            String[] parts = expireDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            int fullYear = 2000 + year;

            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();

            if (fullYear < currentYear) {
                throw new IllegalArgumentException("Karta muddati o'tgan! Yil: " + fullYear);
            }

            if (fullYear == currentYear && month < currentMonth) {
                throw new IllegalArgumentException("Karta muddati o'tgan! Oy: " + month);
            }

            log.info("✅ Karta muddati to'g'ri: {}/{}", month, fullYear);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Noto'g'ri ma'lumot!");
        }
    }

    // ============================================
    // KARTA MUDDATINI TEKSHIRISH (BOOL QAYTARADI)
    // ============================================
    public boolean isCardExpired(String expireDate) {
        try {
            String[] parts = expireDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            int fullYear = 2000 + year;
            LocalDate now = LocalDate.now();

            return fullYear < now.getYear() || (fullYear == now.getYear() && month < now.getMonthValue());
        } catch (Exception e) {
            return true;
        }
    }

    // ============================================
    // KARTALAR RO'YXATI
    // ============================================
    @Transactional(readOnly = true)
    public List<BankCard> getMyCards(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return bankCardRepository.findByJobSeekerId(profile.getId());
    }

    // ============================================
    // TRANZAKSIYALAR TARIXI
    // ============================================
    @Transactional(readOnly = true)
    public List<WalletTransaction> getTransactionHistory(Long userId) {
        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));
        return transactionRepository.findByJobSeekerIdOrderByCreatedAtDesc(profile.getId());
    }

    // ============================================
    // PUL YECHISH
    // ============================================
    @Transactional
    public String withdrawMoney(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Miqdor 0 dan katta bo'lishi kerak!");
        }

        JobSeekerProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ishchi profili topilmadi: " + userId));

        List<BankCard> cards = bankCardRepository.findByJobSeekerId(profile.getId());
        if (cards.isEmpty()) {
            throw new IllegalStateException("Pul yechish uchun avval karta qo'shing!");
        }

        for (BankCard card : cards) {
            if (isCardExpired(card.getExpireDate())) {
                throw new IllegalStateException("Karta muddati o'tgan! " + card.getCardNumber());
            }
        }

        if (profile.getWalletBalance() == null || profile.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Hisobda yetarli mablag' mavjud emas! Balans: " + profile.getWalletBalance());
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
        log.info("💸 Pul yechildi: userId={}, amount={}", userId, amount);

        return "Pul yechish muvaffaqiyatli! Yechilgan summa: " + amount;
    }


}