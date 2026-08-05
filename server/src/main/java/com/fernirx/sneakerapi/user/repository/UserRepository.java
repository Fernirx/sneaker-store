package com.fernirx.sneakerapi.user.repository;

import com.fernirx.sneakerapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE deleted_at IS NOT NULL", 
           countQuery = "SELECT count(*) FROM users WHERE deleted_at IS NOT NULL", 
           nativeQuery = true)
    org.springframework.data.domain.Page<User> findAllDeleted(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "UPDATE users SET deleted_at = NULL, active = 1 WHERE id = :id", nativeQuery = true)
    void restoreUserNative(@Param("id") Long id);
}