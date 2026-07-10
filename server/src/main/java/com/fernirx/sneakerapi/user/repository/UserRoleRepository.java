package com.fernirx.sneakerapi.user.repository;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteAllByUser(User user);

    Optional<UserRole> findByUserAndRole(User user, Role role);

    @Query("SELECT ur.user.id FROM UserRole ur WHERE ur.role = :role")
    List<Long> findUserIdsByRole(@Param("role") Role role);
}