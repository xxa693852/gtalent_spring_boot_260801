package student.jia.gtalent_spring_boot_260801.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank; //不能空白
import jakarta.validation.constraints.NotNull; //不能為空值
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;

@Getter
@Setter
public class MemberRegisterRequest {
    
    @NotBlank(message = ResponseMessages.MEMBER_NAME_REQUIRED) //不能為空白
    @Size(max = 30, message = ResponseMessages.MEMBER_NAME_MAX)
    private String name;

    @NotBlank(message = ResponseMessages.MEMBER_ACCOUNT_REQUIRED)
    @Size(max = 30, message = ResponseMessages.MEMBER_ACCOUNT_MAX)
    private String account;

    @NotBlank(message = ResponseMessages.MEMBER_PASSWORD_REQUIRED)
    @Size(max = 12, min = 6, message = ResponseMessages.MEMBER_PASSWORD_SIZE)
    private String password;

    @NotBlank(message = ResponseMessages.MEMBER_CONFIRM_PASSWORD_REQUIRED)
    private String confirmPassword; //確認密碼

    @Email(message = ResponseMessages.MEMBER_EMAIL_INVALID)
    @Size(max = 128, message = ResponseMessages.MEMBER_EMAIL_MAX)
    private String email;

    @NotNull(message = ResponseMessages.MEMBER_GENDER_REQUIRED)
    @Min(value = 0, message = ResponseMessages.MEMBER_GENDER_INVALID)
    @Max(value = 2, message = ResponseMessages.MEMBER_GENDER_INVALID)
    private Byte gender; //性別
}