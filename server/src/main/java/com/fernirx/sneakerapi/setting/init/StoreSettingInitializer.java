package com.fernirx.sneakerapi.setting.init;

import com.fernirx.sneakerapi.setting.entity.StoreSetting;
import com.fernirx.sneakerapi.setting.repository.StoreSettingRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class StoreSettingInitializer implements ApplicationRunner {
    private final StoreSettingRepository storeSettingRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) throws Exception {
        if (storeSettingRepository.count() > 0) {
            return;
        }
        StoreSetting entity = new StoreSetting();
        entity.setPointsPerAmount(BigDecimal.valueOf(10000));
        entity.setFreeShipThreshold(BigDecimal.valueOf(1_000_000));
        entity.setSilverThreshold(BigDecimal.valueOf(5_000_000));
        entity.setGoldThreshold(BigDecimal.valueOf(15_000_000));
        entity.setPlatinumThreshold(BigDecimal.valueOf(30_000_000));
        storeSettingRepository.save(entity);
    }
}
