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

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.customer"));
        return addressRepository.findByCustomerIdOrderByDefaultAddressDescCreatedAtAsc(customer.getId())
                .stream().map(addressMapper::toResponse).toList();
    }

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

    @Override
    public AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        Address address = findAddressOwned(userId, addressId);
        if (Boolean.TRUE.equals(request.defaultAddress())) {
            addressRepository.clearDefaultByCustomerId(address.getCustomer().getId());
        }
        addressMapper.updateAddress(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findAddressOwned(userId, addressId);
        addressRepository.delete(address);
    }

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
