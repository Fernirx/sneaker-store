package com.fernirx.sneakerapi.setting.service;

import com.fernirx.sneakerapi.setting.dto.request.CreateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.request.UpdateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;

public interface SettingService {
    StoreSettingResponse getStoreSetting();
    StoreSettingResponse createStoreSetting(CreateStoreSettingRequest request);
    StoreSettingResponse updateStoreSetting(UpdateStoreSettingRequest request);
}
