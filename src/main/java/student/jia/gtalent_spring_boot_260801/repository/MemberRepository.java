package student.jia.gtalent_spring_boot_260801.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import student.jia.gtalent_spring_boot_260801.entity.Member;
//extends JpaRepository<Member, Long> 它告訴 Spring Data JPA：「這個 Repository 是拿來操作 Member Entity 的。」
//extends JpaRepository<Member, Long>這句讓 Repository 成為 JPA Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    //@Query 告訴 Spring Data JPA 使用自訂 SQL 語句而不是自動產生語句
    @Query(
        value="SELECT COUNT(*) FROM members WHERE account = :account",
                nativeQuery = true
    )
    // 去資料庫內搜尋這個帳號的數量
    public long countByAccount(@Param("account") String account);


    @Query(
        value="SELECT * FROM members WHERE id = :id and status = 1", // SELECT * FROM members WHERE id = 4 and status = 1"
                nativeQuery = true
    )
    // 找出單一會員By Id
    public Optional<Member> findOneById(@Param("id") long id);


    // 會員登入用 account 查詢，status = 1 才能登入。
    @Query(
            value = "SELECT * FROM members WHERE account = :account AND status = 1", //SELECT * FROM members WHERE account = 'hello456' AND status = 1 (Mysql內的帳號用''單引號包起來)
            nativeQuery = true
    )
    // Optional<>用來包裝資料的「容器物件」。它的主要目的是明確表示一個方法「可能回傳資料，也可能沒有資料（空值）」，藉此取代傳統容易引發錯誤的 null，並減少「空指標例外（NullPointerException）」的發生
    public Optional<Member> findOneByAccountAndStatus(
            @Param("account") String account
    );
}