package uz.imaan.jobplatform.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



public record SettingsUpdateRequest (

    String language,
    Boolean notificationsEnabled,
    Boolean profileHidden

){}
