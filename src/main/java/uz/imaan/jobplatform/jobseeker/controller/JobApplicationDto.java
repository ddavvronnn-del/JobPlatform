package uz.imaan.jobplatform.jobseeker.controller;

import jdk.jshell.Snippet;
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
    private String status;         // Masalan: "PENDING", "ACCEPTED", "REJECTED"
    private LocalDateTime appliedAt;

}
