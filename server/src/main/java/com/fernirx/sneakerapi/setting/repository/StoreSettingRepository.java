package com.fernirx.sneakerapi.setting.repository;

import com.fernirx.sneakerapi.setting.entity.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreSettingRepository extends JpaRepository<StoreSetting, Long> {
    Optional<StoreSetting> findFirstByOrderByIdAsc();
}
