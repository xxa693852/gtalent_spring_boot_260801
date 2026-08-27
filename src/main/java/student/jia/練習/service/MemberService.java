package student.jia.練習.service;





import student.jia.練習.repository.MemberRepository;
import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;
import student.jia.gtalent_spring_boot_260801.exception.ResourceNotFoundException;
import student.jia.練習.entity.Member;


import java.util.Optional;

public class MemberService {
    private MemberRepository repository;
    
    public MemberService(MemberRepository repository){
        this.repository = repository;
    }

    public Member findOneById(Long id){
    // 1.給repository找
    Optional<Member> member = this.repository.findOneById(id);
    // 2.如果找不到
    if(member.isEmpty()){
        throw new  ResourceNotFoundException("member", ResponseMessages.MEMBER_NOT_FOUND);
    }
    return member.get();
    }

    




}
