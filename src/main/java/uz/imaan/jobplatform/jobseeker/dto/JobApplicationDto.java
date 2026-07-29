package uz.imaan.jobplatform.jobseeker.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobApplicationDto {
    private Long id;
    private Long jobId;
    private Long jobSeekerId;
    private String status;
    private LocalDateTime appliedAt;
    private String coverLetter;
}
