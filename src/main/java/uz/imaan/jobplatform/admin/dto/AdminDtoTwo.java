package uz.imaan.jobplatform.admin.dto;

public record AdminDtoTwo(
        Long id,Long telegramId,
        String username,
        String role,
        Boolean isActive,
        Long NumberOfUsers,
        Long NumberOfRequests,
        Long totalWorkers,
        Long totalEmployers,
        long completedShifts,
        Long activeJobs,
        Long completedJobs,
        Long totalAdmins,
        Long userId,
        String reason,
        String Email,
        String Password,
        Long totalJobs,
        long totalVacancies,
        long activeShifts

) {
}
