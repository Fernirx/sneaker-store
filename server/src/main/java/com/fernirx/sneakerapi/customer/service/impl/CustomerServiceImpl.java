package com.fernirx.sneakerapi.customer.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import com.fernirx.sneakerapi.customer.mapper.CustomerMapper;
import com.fernirx.sneakerapi.customer.repository.CustomerRepository;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public void initCustomer(User user) {
        if (customerRepository.findByUserId(user.getId()).isPresent()) return;
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setLoyaltyPoints(0L);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setMembershipTier(MembershipTier.BRONZE);
        customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        return customerMapper.toResponse(customer);
    }
}
