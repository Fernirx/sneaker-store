package com.fernirx.sneakerapi.user.repository;

import com.fernirx.sneakerapi.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {}