package student.jia.練習.response;



import java.time.LocalDateTime;

import lombok.Getter;
import student.jia.練習.entity.Member123;

@Getter
public class MemberResponse123 {

    private Long id;
    private String name;
    private Byte gender;
    private String account;
    private String email;
    private Byte status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MemberResponse123(Member123 member) {
        this.id = member.getId();
        this.name = member.getName();
        this.gender = member.getGender();
        this.account = member.getAccount();
        this.email = member.getEmail();
        this.status = member.getStatus();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdateAt();
    }
}