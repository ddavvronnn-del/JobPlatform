package uz.imaan.jobplatform.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsUpdateRequest {

    private String language;
    private Boolean notificationsEnabled;
    private Boolean profileHidden;

}
