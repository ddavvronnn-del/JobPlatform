package uz.imaan.jobplatform.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDto {

    private Long id;
    private String title;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profession;
    private String experience;
    private String education;
    private String skills;
    private String about;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
