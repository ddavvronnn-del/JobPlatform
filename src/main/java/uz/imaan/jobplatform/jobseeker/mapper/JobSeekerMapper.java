package uz.imaan.jobplatform.jobseeker.mapper;


import uz.imaan.jobplatform.jobseeker.dto.UpdateProfileRequest;
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

    public void updateEntityFromDto( UpdateProfileRequest dto, JobSeekerProfile entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Faqat tahrirlanishi mumkin bo'lgan maydonlarni yangilaymiz
        if (dto.getFullName() != null) {
            entity.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            entity.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getProfession() != null) {
            entity.setProfession(dto.getProfession());
        }
        if (dto.getLanguage() != null) {
            entity.setLanguage(dto.getLanguage());
        }
        if (dto.getPortfolio() != null) {
            entity.setPortfolio(dto.getPortfolio());
        }
    }

}