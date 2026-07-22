package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyJobRequest {

    @NotNull(message = "Job ID bo'sh bo'lishi mumkin emas")
    private Long jobId;

    private String coverLetter;

}
