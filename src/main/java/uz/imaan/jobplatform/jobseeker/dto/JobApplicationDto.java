package uz.imaan.jobplatform.jobseeker.dto;


import lombok.*;

import java.time.LocalDateTime;


public record JobApplicationDto (
     Long id,
     Long jobId,
     Long jobSeekerId,
     String status,
     LocalDateTime appliedAt,
     String coverLetter
){}
