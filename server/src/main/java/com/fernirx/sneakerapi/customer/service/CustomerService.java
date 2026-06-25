package com.fernirx.sneakerapi.customer.service;

import com.fernirx.sneakerapi.customer.dto.request.UpdateCustomerRequest;
import com.fernirx.sneakerapi.customer.dto.request.CustomerFilterRequest;
import com.fernirx.sneakerapi.customer.dto.response.CustomerInternalResponse;
import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CustomerService {
    void initCustomer(User user);
    Customer getOrCreateByUserId(Long userId);
    CustomerResponse getCustomer(Long userId);
    Page<CustomerInternalResponse> getCustomers(CustomerFilterRequest filter, Pageable pageable);
    CustomerInternalResponse getCustomerById(Long id);
    CustomerInternalResponse updateCustomer(Long id, UpdateCustomerRequest request);
    void deleteCustomer(Long id);
    void earnFromOrder(Long customerId, Long orderId, BigDecimal earnedAmount);
    void revokeFromOrder(Long customerId, Long orderId, BigDecimal revokedAmount);
}
