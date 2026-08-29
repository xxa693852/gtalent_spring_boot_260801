package student.jia.gtalent_spring_boot_260801.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import student.jia.gtalent_spring_boot_260801.request.MemberPasswordUpdateRequest;
import student.jia.gtalent_spring_boot_260801.request.MemberProfileUpdateRequest;
import student.jia.gtalent_spring_boot_260801.request.MemberForgotPasswordRequest;
import student.jia.gtalent_spring_boot_260801.request.MemberPasswordResetRequest;
import student.jia.gtalent_spring_boot_260801.request.MemberRegisterRequest;
import student.jia.gtalent_spring_boot_260801.response.ApiResponse;

import student.jia.gtalent_spring_boot_260801.response.MemberResponse;
import student.jia.gtalent_spring_boot_260801.response.PageResponse;
import student.jia.gtalent_spring_boot_260801.response.TokenResponse;
import student.jia.gtalent_spring_boot_260801.service.MemberService;
import student.jia.gtalent_spring_boot_260801.repository.MemberRepository;

import student.jia.gtalent_spring_boot_260801.request.MemberLoginRequest;

import student.jia.gtalent_spring_boot_260801.request.TokenLogoutRequest;



@RestController //這個 Class 是一個 Web API 控制器；告訴 Spring 這個類別專門用來接收 HTTP 請求（如 GET、POST），並且直接把執行的結果「轉換成資料（如 JSON 或 XML）」傳回給前端，而不是去渲染 HTML 網頁
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService,MemberRepository repository) {
        this.memberService = memberService;

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK) // 指定此 API 成功回應的 HTTP 狀態碼為 200 OK
    // @PathVariable : 從 URL 路徑擷取目標會員 ID
    public MemberResponse getOneById(@PathVariable Long id) {
        return new MemberResponse(memberService.findOneById(id));
    }
    //查詢全部
    @GetMapping
    public PageResponse<MemberResponse> getAll(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) { 
        // 預設頁碼從1開始
        if(page<1){
            page = 1;
        }
        // 每頁最少數量不能為0
        // 如果帶0進來, 自動呈現1頁10組
        if(size <1){
            size = 10;
        }
        // 每頁最大不能超過50組
        if(size >50){
            size = 50;
        }
        return memberService.findAll(page, size);
    }
    

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.OK)
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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse logout(@Valid @RequestBody TokenLogoutRequest request) {
        memberService.logout(request.getRefreshToken());
        return new ApiResponse("會員登出成功");
    }

    // 課後練習:
    // 1. get members 取得所有會員 且 做分頁功能

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse forgotPassword(@Valid @RequestBody MemberForgotPasswordRequest request){
        memberService.forgotPassword(request);
        return new ApiResponse("若帳號存在，將寄送重設密碼信");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse resetPassword(@Valid @RequestBody MemberPasswordResetRequest request){
        memberService.resetPassword(request);
        return new ApiResponse("密碼重設成功，請重新登入");
    }

}