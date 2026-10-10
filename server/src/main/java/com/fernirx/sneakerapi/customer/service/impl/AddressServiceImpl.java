package com.fernirx.sneakerapi.customer.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.customer.dto.request.CreateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.request.UpdateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.response.AddressResponse;
import com.fernirx.sneakerapi.customer.entity.Address;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.mapper.AddressMapper;
import com.fernirx.sneakerapi.customer.repository.AddressRepository;
import com.fernirx.sneakerapi.customer.repository.CustomerRepository;
import com.fernirx.sneakerapi.customer.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    /**
     * Lấy danh sách sổ địa chỉ của khách hàng.
     * Sắp xếp: Địa chỉ mặc định (defaultAddress = true) luôn nằm trên cùng.
     * Các địa chỉ còn lại sắp xếp theo thời gian tạo cũ nhất.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        return addressRepository.findByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(customer.getId())
                .stream().map(addressMapper::toResponse).toList();
    }

    /**
     * Thêm mới một địa chỉ vào sổ địa chỉ.
     * Luồng xử lý:
     * 1. Tìm thông tin Customer tương ứng với User.
     * 2. Nếu người dùng chọn đây là địa chỉ mặc định -> Gọi lệnh clear (hủy mặc định) 
     *    tất cả các địa chỉ cũ của khách hàng này.
     * 3. Lưu địa chỉ mới.
     */
    @Override
    public AddressResponse createAddress(Long userId, CreateAddressRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        if (Boolean.TRUE.equals(request.defaultAddress())) {
            addressRepository.clearDefaultByCustomerId(customer.getId());
        }
        Address address = addressMapper.toAddress(request);
        address.setCustomer(customer);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    /**
     * Cập nhật thông tin địa chỉ đã lưu.
     * Luồng xử lý:
     * 1. Kiểm tra quyền sở hữu: Phải đúng địa chỉ của Customer này.
     * 2. Nếu cập nhật thành địa chỉ mặc định -> Gọi lệnh clear các địa chỉ khác.
     * 3. Lưu lại thông tin mới.
     */
    @Override
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        Address address = findAddressOwned(userId, addressId);
        if (Boolean.TRUE.equals(request.defaultAddress())) {
            addressRepository.clearDefaultByCustomerId(address.getCustomer().getId());
        }
        addressMapper.updateAddress(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    /**
     * Xóa một địa chỉ khỏi sổ địa chỉ.
     * Luồng xử lý: Xác thực quyền sở hữu và xóa cứng khỏi hệ thống.
     */
    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findAddressOwned(userId, addressId);
        addressRepository.delete(address);
    }

    /**
     * Xác thực quyền sở hữu địa chỉ: Đảm bảo địa chỉ thuộc về chính Customer hiện tại.
     * Chống lỗi bảo mật Insecure Direct Object Reference (IDOR).
     */
    private Address findAddressOwned(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> BusinessException.notFound("label.address"));
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw SecurityCustomException.forbidden();
        }
        return address;
    }
}
