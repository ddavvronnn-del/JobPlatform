package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankCardRequest {

    @NotBlank(message = "Karta raqami kiritilishi shart")
    @Pattern(regexp = "^[0-9]{16}$", message = "Karta raqami 16 ta raqamdan iborat bo'lishi kerak")
    private String cardNumber;

    @NotBlank(message = "Amal qilish muddati kiritilishi shart")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Format: MM/YY (masalan: 12/26)")
    private String expireDate;

    @NotBlank(message = "Karta egasi ismi kiritilishi shart" )
    private String cardHolderName; // Ali Valiyev

}
