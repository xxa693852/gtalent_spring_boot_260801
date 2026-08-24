package student.jia.gtalent_spring_boot_260801.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import student.jia.gtalent_spring_boot_260801.request.MemberPasswordUpdateRequest;
import student.jia.gtalent_spring_boot_260801.request.MemberProfileUpdateRequest;
import student.jia.gtalent_spring_boot_260801.request.MemberRegisterRequest;
import student.jia.gtalent_spring_boot_260801.response.ApiResponse;
import student.jia.gtalent_spring_boot_260801.response.MemberResponse;
import student.jia.gtalent_spring_boot_260801.response.TokenResponse;
import student.jia.gtalent_spring_boot_260801.service.MemberService;

import student.jia.gtalent_spring_boot_260801.request.MemberLoginRequest;
import student.jia.gtalent_spring_boot_260801.request.TokenLogoutRequest;
import student.jia.gtalent_spring_boot_260801.request.TokenRefreshRequest;

@RestController //這個 Class 是 API Controller
@RequestMapping("/members") //代表所有 API 前面都加：members
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{id}") //GET /members/1(id)
    @ResponseStatus(HttpStatus.OK) //預設回傳200
    public MemberResponse getOneById(@PathVariable Long id) {
        return new MemberResponse(memberService.findOneById(id));
    }

    @PostMapping("/register") //POST /members/register
    @ResponseStatus(HttpStatus.CREATED) // REST 控制器方法上使用的 Java 註解，用於配置端點，使其在請求成功時傳回HTTP 狀態碼 201（已建立）而不是預設的 200（已確定）。
    //@Valid 用來啟動MemberRegisterRequest裡的驗證規則
    public ApiResponse register(@Valid @RequestBody MemberRegisterRequest request) {
        memberService.register(request);
        return new ApiResponse("會員註冊成功");
    }

    // 修改 name gender email
    // 有帶參數才修改, 沒帶就是維持原本
    @PutMapping("/{id}/profile")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody MemberProfileUpdateRequest request) {
        memberService.updateProfile(id, request);
        return new ApiResponse("會員基本資料修改成功");
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.OK) //用於將控制器方法或自訂異常的HTTP 回應狀態碼明確設定為 200 OK 的註解
    public ApiResponse updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody MemberPasswordUpdateRequest request) {
        memberService.updatePassword(id, request);
        return new ApiResponse("會員密碼修改成功");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse delete(@PathVariable Long id) {
        memberService.delete(id);
        return new ApiResponse("會員帳號刪除成功");
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse login(@Valid @RequestBody MemberLoginRequest request) {
        return memberService.login(request);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return memberService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse logout(@Valid @RequestBody TokenLogoutRequest request) {
        memberService.logout(request.getRefreshToken());
        return new ApiResponse("會員登出成功");
    }

    // 課後練習:
    // 1. get members 取得所有會員 且 做分頁功能
}