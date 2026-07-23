package uz.imaan.jobplatform.jobseeker.mapper;

import uz.imaan.jobplatform.jobseeker.dto.JobSeekerProfileDto;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import org.springframework.stereotype.Component;

@Component
public class JobSeekerMapper {

    public JobSeekerProfileDto toDto(JobSeekerProfile entity) {
        if (entity == null) {
            return null;
        }
        return JobSeekerProfileDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .profession(entity.getProfession())
                .rating(entity.getRating())
                .language(entity.getLanguage())
                .portfolio(entity.getPortfolio())
                .walletBalance(entity.getWalletBalance())
                .build();
    }

}