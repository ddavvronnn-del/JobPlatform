package uz.imaan.jobplatform.admin.dto;

import lombok.Data;
    @Data
    public  class AdminDTO {
        private Long id;
        private Long telegramId;
        private String username;
        private String role;
        private Boolean isActive;
    }

