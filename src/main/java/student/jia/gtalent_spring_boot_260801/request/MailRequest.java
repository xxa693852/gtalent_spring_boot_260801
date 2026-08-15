package student.jia.gtalent_spring_boot_260801.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;

/**
 * Maps the JSON body sent to POST /mail/send.
 */
public class MailRequest {

    @NotBlank(message = ResponseMessages.MAIL_TO_REQUIRED)
    @Email(message = ResponseMessages.MAIL_TO_INVALID)
    private String to;

    @NotBlank(message = ResponseMessages.MAIL_SUBJECT_REQUIRED)
    private String subject;

    @NotBlank(message = ResponseMessages.MAIL_TEXT_REQUIRED)
    private String text;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
