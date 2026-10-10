package com.fernirx.sneakerapi.customer.repository;

import com.fernirx.sneakerapi.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByUserId(Long userId);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    @Modifying
    @Query(value = "UPDATE customers SET deleted_at = NULL WHERE user_id = :userId", nativeQuery = true)
    void restoreCustomerNative(@Param("userId") Long userId);
}
