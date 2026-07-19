package com.fernirx.sneakerapi.customer.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.customer.dto.request.UpdateCustomerRequest;
import com.fernirx.sneakerapi.customer.dto.request.CustomerFilterRequest;
import com.fernirx.sneakerapi.customer.dto.response.CustomerInternalResponse;
import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.entity.PointTransaction;
import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import com.fernirx.sneakerapi.customer.enums.PointReferenceType;
import com.fernirx.sneakerapi.customer.enums.PointTransactionType;
import com.fernirx.sneakerapi.customer.mapper.CustomerMapper;
import com.fernirx.sneakerapi.customer.repository.CustomerRepository;
import com.fernirx.sneakerapi.customer.repository.CustomerSpec;
import com.fernirx.sneakerapi.customer.repository.PointTransactionRepository;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;
import com.fernirx.sneakerapi.setting.service.SettingService;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;
    private final SettingService settingService;

    @Override
    public Customer getOrCreateByUserId(Long userId) {
        return customerRepository.findByUserId(userId).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setUser(userRepository.getReferenceById(userId));
            customer.setLoyaltyPoints(0L);
            customer.setTotalSpent(BigDecimal.ZERO);
            customer.setMembershipTier(MembershipTier.BRONZE);
            return customerRepository.save(customer);
        });
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerInternalResponse> getCustomers(CustomerFilterRequest filter, Pageable pageable) {
        return customerRepository.findAll(CustomerSpec.build(filter), pageable)
                .map(customerMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerInternalResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        return customerMapper.toInternalResponse(customer);
    }

    @Override
    public CustomerInternalResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        customerMapper.updateCustomer(request, customer);
        return customerMapper.toInternalResponse(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(Long id) {
        // Soft-delete (xem @SQLDelete trên Customer entity) - Order/PointTransaction vẫn giữ nguyên liên
        // kết đầy đủ để đối soát/khôi phục sau này, không cần pre-check chặn như hard-delete trước đây.
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        customerRepository.delete(customer);
    }

    @Override
    public void earnFromOrder(Long customerId, Long orderId, BigDecimal earnedAmount) {
        if (earnedAmount == null || earnedAmount.signum() <= 0) return;

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));

        if (pointTransactionRepository.existsByCustomerAndReferenceTypeAndReferenceIdAndType(
                customer, PointReferenceType.ORDER, orderId, PointTransactionType.EARN)) {
            return;
        }

        long earnedPoints = earnedAmount.divideToIntegralValue(settingService.getStoreSetting().pointsPerAmount()).longValue();

        PointTransaction tx = new PointTransaction();
        tx.setCustomer(customer);
        tx.setAmount(earnedPoints);
        tx.setType(PointTransactionType.EARN);
        tx.setReferenceType(PointReferenceType.ORDER);
        tx.setReferenceId(orderId);
        tx.setNote("Tích điểm từ đơn hàng #" + orderId);
        pointTransactionRepository.save(tx);

        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + earnedPoints);
        customer.setTotalSpent(customer.getTotalSpent().add(earnedAmount));

        MembershipTier naturalTier = resolveTier(customer.getTotalSpent());
        if (naturalTier.ordinal() > customer.getMembershipTier().ordinal()) {
            customer.setMembershipTier(naturalTier);
        }

        customerRepository.save(customer);
    }

    @Override
    public void revokeFromOrder(Long customerId, Long orderId, BigDecimal revokedAmount) {
        if (revokedAmount == null || revokedAmount.signum() <= 0) return;

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));

        if (pointTransactionRepository.existsByCustomerAndReferenceTypeAndReferenceIdAndType(
                customer, PointReferenceType.ORDER, orderId, PointTransactionType.REVOKE)) {
            return;
        }

        if (!pointTransactionRepository.existsByCustomerAndReferenceTypeAndReferenceIdAndType(
                customer, PointReferenceType.ORDER, orderId, PointTransactionType.EARN)) {
            return;
        }

        long revokedPoints = revokedAmount.divideToIntegralValue(settingService.getStoreSetting().pointsPerAmount()).longValue();

        PointTransaction tx = new PointTransaction();
        tx.setCustomer(customer);
        tx.setAmount(revokedPoints);
        tx.setType(PointTransactionType.REVOKE);
        tx.setReferenceType(PointReferenceType.ORDER);
        tx.setReferenceId(orderId);
        tx.setNote("Thu hồi điểm do hủy/trả đơn hàng #" + orderId);
        pointTransactionRepository.save(tx);

        customer.setLoyaltyPoints(Math.max(0, customer.getLoyaltyPoints() - revokedPoints));
        customer.setTotalSpent(customer.getTotalSpent().subtract(revokedAmount).max(BigDecimal.ZERO));

        MembershipTier naturalTier = resolveTier(customer.getTotalSpent());
        if (naturalTier.ordinal() < customer.getMembershipTier().ordinal()) {
            customer.setMembershipTier(naturalTier);
        }

        customerRepository.save(customer);
    }

    @Override
    public void revokePartial(Long customerId, Long orderId, Long returnRequestId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return;

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));

        if (!pointTransactionRepository.existsByCustomerAndReferenceTypeAndReferenceIdAndType(
                customer, PointReferenceType.ORDER, orderId, PointTransactionType.EARN)) {
            return;
        }
        if (pointTransactionRepository.existsByCustomerAndReferenceTypeAndReferenceIdAndType(
                customer, PointReferenceType.RETURN_REQUEST, returnRequestId, PointTransactionType.REVOKE)) {
            return;
        }

        long revokedPoints = amount.divideToIntegralValue(settingService.getStoreSetting().pointsPerAmount()).longValue();

        PointTransaction tx = new PointTransaction();
        tx.setCustomer(customer);
        tx.setAmount(revokedPoints);
        tx.setType(PointTransactionType.REVOKE);
        tx.setReferenceType(PointReferenceType.RETURN_REQUEST);
        tx.setReferenceId(returnRequestId);
        tx.setNote("Thu hồi điểm do đổi/trả một phần đơn hàng #" + orderId);
        pointTransactionRepository.save(tx);

        customer.setLoyaltyPoints(Math.max(0, customer.getLoyaltyPoints() - revokedPoints));
        customer.setTotalSpent(customer.getTotalSpent().subtract(amount).max(BigDecimal.ZERO));

        MembershipTier naturalTier = resolveTier(customer.getTotalSpent());
        if (naturalTier.ordinal() < customer.getMembershipTier().ordinal()) {
            customer.setMembershipTier(naturalTier);
        }

        customerRepository.save(customer);
    }

    private MembershipTier resolveTier(BigDecimal totalSpent) {
        StoreSettingResponse storeSetting = settingService.getStoreSetting();
        if (totalSpent.compareTo(storeSetting.platinumThreshold()) >= 0) return MembershipTier.PLATINUM;
        if (totalSpent.compareTo(storeSetting.goldThreshold()) >= 0) return MembershipTier.GOLD;
        if (totalSpent.compareTo(storeSetting.silverThreshold()) >= 0) return MembershipTier.SILVER;
        return MembershipTier.BRONZE;
    }
}
