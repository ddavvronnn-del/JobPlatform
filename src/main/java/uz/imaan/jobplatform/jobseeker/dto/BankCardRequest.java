package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record BankCardRequest (

    @NotBlank(message = "Karta raqami kiritilishi shart")
    @Pattern(regexp = "^[0-9]{16}$", message = "Karta raqami 16 ta raqamdan iborat bo'lishi kerak")
    String cardNumber,


    @NotBlank(message = "Karta egasi ismi kiritilishi shart" )
    String cardHolderName // Ali Valiyev

){}
