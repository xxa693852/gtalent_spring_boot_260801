package student.jia.gtalent_spring_boot_260801.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import student.jia.gtalent_spring_boot_260801.request.MailRequest;
import student.jia.gtalent_spring_boot_260801.service.MailService;

/** 提供手動測試寄信的 API。 */
@RestController
public class MailController {

    // final 表示建構後不可被重新指定，避免 Service 被意外替換。
    private final MailService mailService;

    // Spring 會自動注入 MailService，Controller 再交給它負責寄信。
    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * 手動寄送一封純文字信件。
     *
     * @param request 前端傳來的 JSON，包含 to、subject、text
     * @return 寄送結果文字
     */
    @PostMapping("/mail/send")
    public String sendEmail(@Valid @RequestBody MailRequest request) {
        // @Valid 會先確認欄位不是空白、收件人格式正確；驗證通過才會執行到這裡。
        // Controller 不直接處理 Gmail 細節，交給 MailService，方便維護與重複使用。
        mailService.sendEmail(request.getTo(), request.getSubject(), request.getText());
        return "信件寄送成功";
    }

    // 練習 1：可嘗試改成從 @RequestParam 取得 to、subject、text。
    // 練習 2：書籍新增、修改、刪除時，已在 BookController 呼叫 MailService 寄通知。
}
