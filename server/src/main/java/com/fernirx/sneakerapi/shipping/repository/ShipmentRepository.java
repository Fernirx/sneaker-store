package com.fernirx.sneakerapi.shipping.repository;

import com.fernirx.sneakerapi.shipping.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    // Mỗi đơn hàng có thể có nhiều dòng shipment theo thời gian (lịch sử các lần tạo/hủy vận đơn) - dòng
    // mới nhất (id lớn nhất) là dòng đang áp dụng cho đơn hàng hiện tại.
    Optional<Shipment> findFirstByOrder_IdOrderByIdDesc(Long orderId);
    List<Shipment> findAllByOrder_IdIn(List<Long> orderIds);
}
