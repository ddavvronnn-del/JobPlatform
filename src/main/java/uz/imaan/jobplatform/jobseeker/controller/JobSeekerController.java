package uz.imaan.jobplatform.jobseeker.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.imaan.jobplatform.jobseeker.dto.ApplyJobRequest;
import uz.imaan.jobplatform.jobseeker.dto.JobSeekerProfileDto;
import uz.imaan.jobplatform.jobseeker.entity.JobApplication;
import uz.imaan.jobplatform.jobseeker.service.JobSeekerService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/job-seeker")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    @GetMapping("/profile")
    public ResponseEntity<JobSeekerProfileDto> getProfile(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(jobSeekerService.getProfileByUserId(userId));
    }

    @PostMapping("/apply")
    public ResponseEntity<JobApplication> applyForJob(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ApplyJobRequest request) {
        return ResponseEntity.ok(jobSeekerService.applyForJob(userId, request));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<JobApplication>> getMyApplications(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(jobSeekerService.getMyApplications(userId));
    }

}
