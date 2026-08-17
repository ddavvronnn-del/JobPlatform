package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;




public record   CreateResumeRequest (

    @NotBlank(message = "Sarlavha kiritilishi shart")
    String title,           // "Java Developer" (majburiy)

    @NotBlank(message = "Ism-familiya kiritilishi shart")
    String fullName,        // "Ali Valiyev" (majburiy)

    String email,           // "ali@gmail.com" (ixtiyoriy)
    String phoneNumber,     // "+998901234567" (ixtiyoriy)
    String profession,      // "Dasturchi" (ixtiyoriy)
    String experience,      // "3 yil Java da ishlagan" (ixtiyoriy)
    String education,       // "TATU, 2020-2024" (ixtiyoriy)
    String skills,          // "Java, Spring, SQL" (ixtiyoriy)
    String about           // "O'zim haqimda..." (ixtiyoriy)

){}
