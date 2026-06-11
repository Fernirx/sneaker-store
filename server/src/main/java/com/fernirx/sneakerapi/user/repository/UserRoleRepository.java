package com.fernirx.sneakerapi.user.repository;

import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteAllByUser(User user);
}