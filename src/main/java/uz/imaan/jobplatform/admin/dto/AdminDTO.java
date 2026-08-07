package uz.imaan.jobplatform.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Data
    public  class AdminDTO {
        private Long id;
        private Long telegramId;
        private String username;
        private String role;
        private Boolean isActive;
        private Long NumberOfUsers;
        private Long NumberOfRequests;
        private Long totalWorkers;
        private Long totalEmployers;
        private Long totalJobs;
        private Long activeJobs;
        private Long completedJobs;
        private Long totalAdmins;
        private Long userId;
        private String reason;


    }


