package com.fernirx.sneakerapi.returns.repository;

import com.fernirx.sneakerapi.returns.entity.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long>, JpaSpecificationExecutor<ReturnRequest> {

    Page<ReturnRequest> findByCustomer_User_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ReturnRequest> findByCustomer_User_IdAndOrder_IdOrderByCreatedAtDesc(Long userId, Long orderId, Pageable pageable);

    Optional<ReturnRequest> findByIdAndCustomer_User_Id(Long id, Long userId);
}
