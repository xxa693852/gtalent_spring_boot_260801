package student.jia.練習.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import student.jia.練習.response.MemberResponse123;
import student.jia.練習.service.MemberService123;

@RestController
@RequestMapping("/members123")
public class MemberController123 {
    
    private final MemberService123 memberService;

    public MemberController123(MemberService123 memberService){
        this.memberService = memberService;
    }

@GetMapping("/{id}")
@ResponseStatus(HttpStatus.OK) // 指定此 API 成功回應的 HTTP 狀態碼為 200 OK
// @PathVariable : 從 URL 路徑擷取目標會員 ID
public MemberResponse123 getOneById(@PathVariable Long id){
    return new MemberResponse123(memberService.findOneById(id));
}
}
