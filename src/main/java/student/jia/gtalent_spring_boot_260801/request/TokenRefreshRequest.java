package student.jia.gtalent_spring_boot_260801.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;

@Getter
@Setter
public class TokenRefreshRequest {
    //@NotBlank註解可確保字串欄位不為空，且包含實際文字
    @NotBlank(message = ResponseMessages.REFRESH_TOKEN_REQUIRED)
    private String refreshToken;
}