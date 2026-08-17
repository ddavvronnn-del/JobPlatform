package uz.imaan.jobplatform.jobseeker.mapper;


import uz.imaan.jobplatform.jobseeker.dto.UpdateProfileRequest;
import uz.imaan.jobplatform.jobseeker.dto.JobSeekerProfileDto;
import uz.imaan.jobplatform.jobseeker.entity.JobSeekerProfile;
import org.springframework.stereotype.Component;

@Component
public class JobSeekerMapper {

    public JobSeekerProfileDto toDto(JobSeekerProfile entity) {
        if (entity == null) return null;

        return new JobSeekerProfileDto(
                entity.getId(),
                entity.getUserId(),
                entity.getFullName(),
                entity.getPhoneNumber(),
                entity.getProfession(),
                entity.getRating(),
                entity.getLanguage(),
                entity.getPortfolio(),
                entity.getWalletBalance(),
                entity.getPassportNumber()
        );
    }

    public void updateEntityFromDto( UpdateProfileRequest dto, JobSeekerProfile entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Faqat tahrirlanishi mumkin bo'lgan maydonlarni yangilaymiz
        if (dto.fullName() != null) {
            entity.setFullName(dto.fullName());
        }
        if (dto.phoneNumber() != null) {
            entity.setPhoneNumber(dto.phoneNumber());
        }
        if (dto.profession() != null) {
            entity.setProfession(dto.profession());
        }
        if (dto.language() != null) {
            entity.setLanguage(dto.language());
        }
        if (dto.portfolio() != null) {
            entity.setPortfolio(dto.portfolio());
        }
        if (dto.passportNumber() != null) {
            entity.setPassportNumber(dto.passportNumber());
        }
    }

}