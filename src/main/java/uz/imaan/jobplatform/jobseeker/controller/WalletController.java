package uz.imaan.jobplatform.jobseeker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.imaan.jobplatform.jobseeker.dto.BankCardRequest;
import uz.imaan.jobplatform.jobseeker.entity.BankCard;
import uz.imaan.jobplatform.jobseeker.entity.WalletTransaction;
import uz.imaan.jobplatform.jobseeker.service.WalletService;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/job-seeker/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/cards")
    public ResponseEntity<BankCard> addCard(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BankCardRequest request) {
        return ResponseEntity.ok(walletService.addBankCard(userId, request));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<BankCard>> getCards(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(walletService.getMyCards(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<WalletTransaction>> getHistory(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawMoney(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.withdrawMoney(userId, amount));
    }

}
