package com.fernirx.sneakerapi.customer.repository;

import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.entity.PointTransaction;
import com.fernirx.sneakerapi.customer.enums.PointReferenceType;
import com.fernirx.sneakerapi.customer.enums.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long>, JpaSpecificationExecutor<PointTransaction> {
    boolean existsByCustomerAndReferenceTypeAndReferenceIdAndType(
            Customer customer, PointReferenceType referenceType, Long referenceId, PointTransactionType type);

    Optional<PointTransaction> findByCustomerAndReferenceTypeAndReferenceIdAndType(
            Customer customer, PointReferenceType referenceType, Long referenceId, PointTransactionType type);
}
