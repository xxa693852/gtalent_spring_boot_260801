package student.jia.gtalent_spring_boot_260801.repository;

import java.util.Optional;
import java.util.List;
import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 已經內建了 List<Member> findAll();
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import student.jia.gtalent_spring_boot_260801.entity.Member;
//extends JpaRepository<Member, Long> 它告訴 Spring Data JPA：「這個 Repository 是拿來操作 Member Entity 的。」
//extends JpaRepository<Member, Long>這句讓 Repository 成為 JPA Repository
//<Member, Long> Member → 我要操作的 Entity
//               Long   → Member 的 ID 型別
public interface MemberRepository extends JpaRepository<Member, Long> {
    //@Query 告訴 Spring Data JPA 使用自訂 SQL 語句而不是自動產生語句
    //@Query 就是在告訴 Spring：「這個 Repository 方法，我不要讓你自己猜怎麼查，我直接告訴你要執行哪一段查詢
    //@Query 指定這個 Repository 方法要用什麼查詢
    @Query(
        value="SELECT COUNT(*) FROM members WHERE account = :account", //:account就是等等會從Java 方法拿一個值進來的位置
                nativeQuery = true  //這一串是「原生 SQL」也就是把上面這段語法交給資料庫，這不是 JPQL，這就是我要直接給資料庫執行的 SQL
                               
    )
    // 去資料庫內搜尋這個帳號的數量
    public long countByAccount(@Param("account") String account);
   /*  @Param("account")  → 告訴 Spring：SQL 裡的 :account
                ↓
        String account     → Java 方法收到的參數*/


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

    

    
    // 會員忘記密碼用 account or email 查詢，status = 1 才能登入。
    @Query(
            value = "SELECT * FROM members WHERE (account = :accountOrEmail OR email = :accountOrEmail) AND status = 1",
            nativeQuery = true
    )
    public Optional<Member> findOneByAccountOrEmailAndStatus(
            @Param("accountOrEmail") String accountOrEmail
    );

    @Query(
        value ="SELECT * FROM members WHERE line_uid = :lineUid AND status = 1",nativeQuery  = true
    )
    Optional<Member> findOneByLineUidAndStatus(
        @Param("lineUid") String lineUid
    );


      List<Member> findByAccountIn(List<String> accounts);
      
}

