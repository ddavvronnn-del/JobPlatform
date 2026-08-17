package uz.imaan.jobplatform.jobseeker.dto;

import jakarta.validation.constraints.NotNull;




public record ApplyJobRequest (

    @NotNull(message = "Job ID kiritilishi shart")
     Long  jobId,
     String comment

){}
