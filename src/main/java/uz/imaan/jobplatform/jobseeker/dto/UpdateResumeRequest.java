package uz.imaan.jobplatform.jobseeker.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResumeRequest {

    private String title;           // Yangi sarlavha
    private String fullName;        // Yangi ism
    private String email;           // Yangi email
    private String phoneNumber;     // Yangi telefon
    private String profession;      // Yangi kasb
    private String experience;      // Yangi tajriba
    private String education;       // Yangi ta'lim
    private String skills;          // Yangi ko'nikmalar
    private String about;           // Yangi "o'zim haqimda"

}
