package student.jia.gtalent_spring_boot_260801.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import student.jia.gtalent_spring_boot_260801.request.MailRequest;
import student.jia.gtalent_spring_boot_260801.service.MailService;

@RestController
public class MailController {
    private MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * 寄送純文字電子郵件。
     *
     * @param request 從 JSON request body 轉換而來的資料
     * @return 寄送成功時回傳的訊息
     */
    @PostMapping("/mail/send")
    public String sendEmail(@Valid @RequestBody MailRequest request) {

        // 將 JSON 的 to、subject、text 交給 Service 實際寄信。
        mailService.sendEmail(request.getTo(), request.getSubject(), request.getText());
        return "郵件發送成功!";
    }


   // 練習1: 改成帶入 to、subject、text 參數 可用post or get

    // 練習2: 新增/修改/刪除書籍 寄送gmail通知
}
