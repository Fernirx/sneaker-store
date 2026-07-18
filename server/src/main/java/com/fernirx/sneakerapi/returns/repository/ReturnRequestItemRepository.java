package com.fernirx.sneakerapi.returns.repository;

import com.fernirx.sneakerapi.returns.entity.ReturnRequest;
import com.fernirx.sneakerapi.returns.entity.ReturnRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnRequestItemRepository extends JpaRepository<ReturnRequestItem, Long> {

    @Query("SELECT i FROM ReturnRequestItem i JOIN FETCH i.orderItem WHERE i.returnRequest = :returnRequest")
    List<ReturnRequestItem> findAllByReturnRequest(@Param("returnRequest") ReturnRequest returnRequest);

    /** Tổng số lượng đã yêu cầu trả cho 1 order_item, tính mọi trạng thái TRỪ REJECTED (kể cả REJECTED_AFTER_INSPECTION vẫn coi là "đã tiêu") — chống trả vượt số lượng đã mua. */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM ReturnRequestItem i " +
            "WHERE i.orderItem.id = :orderItemId " +
            "AND i.returnRequest.status <> com.fernirx.sneakerapi.returns.enums.ReturnStatus.REJECTED")
    int sumQuantityByOrderItemExcludingRejected(@Param("orderItemId") Long orderItemId);
}
