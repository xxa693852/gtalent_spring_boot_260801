package student.jia.練習.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import student.jia.練習.repository.MemberRepository;
import student.jia.練習.response.MemberResponse;
import student.jia.練習.service.MemberService;

@RestController
@RequestMapping("/members123")
public class MemberController {
    
    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

@GetMapping("/{id}")
@RequestMapping(HttpStatus.OK)
public MemberResponse getOneById()
}
