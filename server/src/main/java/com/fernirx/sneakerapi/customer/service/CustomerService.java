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
    void restoreCustomer(Long userId);
    void earnFromOrder(Long customerId, Long orderId, BigDecimal earnedAmount);
    void revokeFromOrder(Long customerId, Long orderId, BigDecimal revokedAmount);

    /**
     * Thu hồi điểm một phần khi đổi/trả hàng (không phải hủy cả đơn). Khác revokeFromOrder: idempotency
     * theo returnRequestId (không phải orderId) để không đụng độ với hủy đơn toàn phần hoặc nhiều lần
     * trả một phần trên cùng 1 đơn - vẫn yêu cầu đơn đã từng earn điểm (kiểm tra theo orderId).
     */
    void revokePartial(Long customerId, Long orderId, Long returnRequestId, BigDecimal amount);
}
