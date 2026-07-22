package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankCardRequest {

    @NotBlank(message = "Karta raqami kiritilishi shart")
    private String cardNumber;

    @NotBlank(message = "Amal qilish muddati kiritilishi shart")
    private String expireDate;

}
