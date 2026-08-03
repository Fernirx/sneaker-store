package com.fernirx.sneakerapi.setting.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.setting.dto.request.CreateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.request.UpdateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;
import com.fernirx.sneakerapi.setting.entity.StoreSetting;
import com.fernirx.sneakerapi.setting.mapper.StoreSettingMapper;
import com.fernirx.sneakerapi.setting.repository.StoreSettingRepository;
import com.fernirx.sneakerapi.setting.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final StoreSettingRepository storeSettingRepository;
    private final StoreSettingMapper storeSettingMapper;

    /**
     * Lấy cấu hình cửa hàng.
     * Sử dụng Redis/Spring Cache (@Cacheable) để tối ưu hiệu suất vì cấu hình rất ít khi thay đổi nhưng được đọc liên tục.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable("store_setting")
    public StoreSettingResponse getStoreSetting() {
        return storeSettingMapper.toResponse(findStoreSetting());
    }

    /**
     * Khởi tạo cấu hình cửa hàng lần đầu tiên.
     * Logic bảo vệ: Bảng cấu hình chỉ được phép có duy nhất 1 dòng dữ liệu (Singleton Record).
     */
    @Override
    @CacheEvict(value = "store_setting", allEntries = true)
    public StoreSettingResponse createStoreSetting(CreateStoreSettingRequest request) {
        if (storeSettingRepository.count() > 0) {
            throw BusinessException.alreadyExists("label.storeSetting");
        }
        StoreSetting entity = storeSettingMapper.toStoreSetting(request);
        storeSettingRepository.save(entity);
        return storeSettingMapper.toResponse(entity);
    }

    /**
     * Cập nhật cấu hình cửa hàng.
     * Tự động xóa Cache (CacheEvict) để các request sau lấy dữ liệu mới nhất từ DB.
     */
    @Override
    @CacheEvict(value = "store_setting", allEntries = true)
    public StoreSettingResponse updateStoreSetting(UpdateStoreSettingRequest request) {
        StoreSetting entity = findStoreSetting();
        storeSettingMapper.updateStoreSetting(request, entity);
        storeSettingRepository.save(entity);
        return storeSettingMapper.toResponse(entity);
    }

    /**
     * Helper tìm cấu hình duy nhất trong DB.
     * Dùng findFirstByOrderByIdAsc thay vì findById(1) để chống lỗi trong trường hợp record bị xóa rồi tạo lại (ID tự nhảy).
     */
    private StoreSetting findStoreSetting() {
        return storeSettingRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> BusinessException.notFound("label.storeSetting"));
    }
}
