package student.jia.練習.service;





import student.jia.練習.repository.MemberRepository123;
import student.jia.練習.response.MemberResponse123;
import student.jia.gtalent_spring_boot_260801.constant.ResponseMessages;
import student.jia.gtalent_spring_boot_260801.exception.ResourceNotFoundException;
import student.jia.練習.entity.Member123;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service  // 必須加上這個註解！告訴 Spring 將它註冊為 Bean
public class MemberService123 {
    private MemberRepository123 repository;
    
    public MemberService123(MemberRepository123 repository){
        this.repository = repository;
    }
    //查詢單一ID
    public Member123 findOneById(Long id){
    // 1.給repository找
    Optional<Member123> member = this.repository.findOneById(id);
    // 2.如果找不到
    if(member.isEmpty()){
        throw new  ResourceNotFoundException("member", ResponseMessages.MEMBER_NOT_FOUND);
    }
    return member.get();
    }
    //查詢全部
    public List<MemberResponse123> findAll(){
        return repository.findAll()
        .stream()
        .map(MemberResponse123::new)// 或是 MemberResponse::from
        .collect(Collectors.toList());
    }
    //查詢全部 另一種寫法
 /*   
    public List<MemberResponse> findAll(){
        List<Member> members = repository.findAll();
        List<MemberResponse> responses = new ArrayList<>();

        for(Member entity:members){
            MemberResponse dto = new MemberResponse(entity);
            responses.add(dto);
        }
        return responses;
    }
*/ 




}
