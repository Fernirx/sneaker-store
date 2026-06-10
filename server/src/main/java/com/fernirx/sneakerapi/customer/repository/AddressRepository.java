package com.fernirx.sneakerapi.customer.repository;

import com.fernirx.sneakerapi.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(Long customerId);

    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.customer.id = :customerId")
    void clearDefaultByCustomerId(Long customerId);
}
