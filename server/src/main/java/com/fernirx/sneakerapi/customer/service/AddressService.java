package com.fernirx.sneakerapi.customer.service;

import com.fernirx.sneakerapi.customer.dto.request.CreateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.request.UpdateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getAddresses(Long userId);
    AddressResponse createAddress(Long userId, CreateAddressRequest request);
    AddressResponse updateAddress(Long userId, Long addressId, UpdateAddressRequest request);
    void deleteAddress(Long userId, Long addressId);
}
