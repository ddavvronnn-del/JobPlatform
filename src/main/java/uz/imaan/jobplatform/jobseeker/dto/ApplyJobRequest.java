package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplyJobRequest {

    @NotNull(message = "Job ID kiritilishi shart")
    private Long  jobId;
    private String comment;


}
