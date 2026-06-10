package com.fernirx.sneakerapi.customer.service;

import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.user.entity.User;

public interface CustomerService {
    void initCustomer(User user);
    CustomerResponse getCustomer(Long userId);
}
