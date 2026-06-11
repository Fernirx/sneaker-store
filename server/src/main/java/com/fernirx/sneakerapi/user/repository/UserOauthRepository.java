package com.fernirx.sneakerapi.user.repository;

import com.fernirx.sneakerapi.user.entity.UserOauth;
import com.fernirx.sneakerapi.user.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOauthRepository extends JpaRepository<UserOauth, Long> {
    boolean existsByProviderAndProviderId(Provider provider, String providerId);
}