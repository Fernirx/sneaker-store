package com.fernirx.sneakerapi.shipping.repository;

import com.fernirx.sneakerapi.shipping.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrder_Id(Long orderId);
    List<Shipment> findAllByOrder_IdIn(List<Long> orderIds);
}
