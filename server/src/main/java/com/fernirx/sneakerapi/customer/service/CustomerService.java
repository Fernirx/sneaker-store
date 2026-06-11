package com.fernirx.sneakerapi.customer.service;

import com.fernirx.sneakerapi.customer.dto.request.UpdateCustomerRequest;
import com.fernirx.sneakerapi.customer.dto.request.CustomerFilterRequest;
import com.fernirx.sneakerapi.customer.dto.response.CustomerInternalResponse;
import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    void initCustomer(User user);
    CustomerResponse getCustomer(Long userId);
    Page<CustomerInternalResponse> getCustomers(CustomerFilterRequest filter, Pageable pageable);
    CustomerInternalResponse getCustomerById(Long id);
    CustomerInternalResponse updateCustomer(Long id, UpdateCustomerRequest request);
    void deleteCustomer(Long id);
}
