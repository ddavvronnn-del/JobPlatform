package admin.dto;

import lombok.Data;

public class AdminDto {

    @Data
    public static class AdminDTO {
        private Long id;
        private Long telegramId;
        private String username;
        private String role;
        private Boolean isActive;
    }
}
