package student.jia.練習.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
/*import jakarta.persistence.Entity;
import jakarta.persistence.Table;
特別是@Entity，他是呼叫JPA管理的關鍵
*/
@Getter
@Setter
@Entity //這句讓 Member 成為 Entity
@Table(name = "members")
public class Member123 {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY) //這個 ID 交給資料庫自動遞增（Auto Increment）來產生，Java 程式不需要（也不應該）手動傳入 ID 值。
private Long id;

@Column(nullable = false,length = 64) //表示不可空白，並且字元上限為64個字
private String name;

@Column(nullable = false)
private Byte gender;

@Column(nullable = false,length = 64 , unique = true) //unique 唯一值，只會有一個
private String account;

@Column(length = 128)
private String email;

@Column(nullable = false,length = 64)
private String password;

@Column(nullable = false)
private Byte status = 1;

@Column(name ="created_at",nullable = false)
private LocalDateTime createdAt;

@Column(name ="updated_at",nullable = false)
private LocalDateTime updateAt;

@Column(name ="deleted_at")
private LocalDateTime deletedAt;

protected Member123(){

}

public Member123(String name, Byte gender, String account, String email, String password) {
    this.name = name;
    this.gender = gender;
    this.account = account;
    this.email = email;
    this.password = password;
}
// @PrePersist: 新建時自動初始化建立與更新時間
@PrePersist
public void prePersist(){
    LocalDateTime now =LocalDateTime.now();
    this.createdAt =now;
    this.updateAt = now;
}
// @PreUpdate: 修改時自動刷新最後更新時間
@PreUpdate
public void preUpdate(){
    this.updateAt = LocalDateTime.now();
}

}