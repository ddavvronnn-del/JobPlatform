package uz.imaan.jobplatform.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerProfileDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String profession;
    private Double rating;
    private String language;
    private String portfolio;
    private BigDecimal walletBalance;

}
