package uz.imaan.jobplatform.jobseeker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.imaan.jobplatform.jobseeker.dto.BankCardRequest;
import uz.imaan.jobplatform.jobseeker.entity.BankCard;
import uz.imaan.jobplatform.jobseeker.entity.WalletTransaction;
import uz.imaan.jobplatform.jobseeker.repository.BankCardRepository;
import uz.imaan.jobplatform.jobseeker.service.interfaces.WalletService;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/job-seeker/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final BankCardRepository bankCardRepository;

    // ============================================
    // 1. KARTA QO'SHISH
    // ============================================
    @PostMapping("/cards")
    public ResponseEntity<BankCard> addCard(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BankCardRequest request) {
        log.info("POST /api/v1/job-seeker/wallet/cards - userId: {}", userId);
        return ResponseEntity.ok(walletService.addBankCard(userId, request));
    }

    // ============================================
    // 2. KARTALAR RO'YXATI
    // ============================================
    @GetMapping("/cards")
    public ResponseEntity<List<BankCard>> getCards(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/v1/job-seeker/wallet/cards - userId: {}", userId);
        return ResponseEntity.ok(walletService.getMyCards(userId));
    }

    // ============================================
    // 3. KARTA MUDDATINI TEKSHIRISH (YANGI QO'SHILDI)
    // ============================================
    @GetMapping("/cards/{cardNumber}/check-expiry")
    public ResponseEntity<Boolean> checkCardExpiry(
            @PathVariable String cardNumber) {
        log.info("GET /api/v1/job-seeker/wallet/cards/{}/check-expiry", cardNumber);

        // Kartani topish
        BankCard card = bankCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Karta topilmadi: " + cardNumber));

        boolean isExpired = walletService.isCardExpired(card.getExpireDate());
        return ResponseEntity.ok(isExpired);
    }

    // ============================================
    // 4. TRANZAKSIYALAR TARIXI
    // ============================================
    @GetMapping("/history")
    public ResponseEntity<List<WalletTransaction>> getHistory(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/v1/job-seeker/wallet/history - userId: {}", userId);
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    // ============================================
    // 5. PUL YECHISH
    // ============================================
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawMoney(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam BigDecimal amount) {
        log.info("POST /api/v1/job-seeker/wallet/withdraw - userId: {}, amount: {}", userId, amount);
        return ResponseEntity.ok(walletService.withdrawMoney(userId, amount));
    }

}
