package student.jia.gtalent_spring_boot_260801.entity;

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
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //這個 ID 交給資料庫自動遞增（Auto Increment）來產生，Java 程式不需要（也不應該）手動傳入 ID 值。
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false)
    private Byte gender;

    @Column(nullable = false, length = 64, unique = true)
    private String account;

    @Column(length = 128)
    private String email;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false)
    private Byte status = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "line_uid", length = 64, unique = true)
    private String lineUid;

    protected Member() {
    }

    public Member(String name, Byte gender, String account, String email, String password) {
        this.name = name;
        this.gender = gender;
        this.account = account;
        this.email = email;
        this.password = password;
    }
    // @PrePersist: 新建時自動初始化建立與更新時間
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    // @PreUpdate: 修改時自動刷新最後更新時間
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}