package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResumeRequest {

    @NotBlank(message = "Sarlavha kiritilishi shart")
    private String title;           // "Java Developer" (majburiy)

    @NotBlank(message = "Ism-familiya kiritilishi shart")
    private String fullName;        // "Ali Valiyev" (majburiy)

    private String email;           // "ali@gmail.com" (ixtiyoriy)
    private String phoneNumber;     // "+998901234567" (ixtiyoriy)
    private String profession;      // "Dasturchi" (ixtiyoriy)
    private String experience;      // "3 yil Java da ishlagan" (ixtiyoriy)
    private String education;       // "TATU, 2020-2024" (ixtiyoriy)
    private String skills;          // "Java, Spring, SQL" (ixtiyoriy)
    private String about;           // "O'zim haqimda..." (ixtiyoriy)

}
