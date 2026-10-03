package student.jia.gtalent_spring_boot_260801.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import student.jia.gtalent_spring_boot_260801.entity.BookOrder;

public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {

    @Query(
        value = "SELECT COUNT(*) FROM book_orders WHERE book_id = :bookId AND order_status = :orderStatus",
        nativeQuery = true
    )
    long countByBookIdAndOrderStatus(
        @Param("bookId") Long bookId,
        @Param("orderStatus") String orderStatus
    );

    @Query(
        value = "SELECT COUNT(*) FROM book_orders WHERE order_no = :orderNo",
        nativeQuery = true
    )
    long countByOrderNo(@Param("orderNo") String orderNo);

    /** 找出已超過指定時間、仍在購買中的訂單。 */
    List<BookOrder> findByBuyerMemberIdAndOrderStatusAndCreatedAtLessThanEqual(
        Long buyerMemberId,
        String orderStatus,
        LocalDateTime createdAt
    );
}
