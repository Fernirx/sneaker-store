package com.fernirx.sneakerapi.user.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(name = "email_UNIQUE",
        columnNames = {"email"})})
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseAuditEntity {
    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @Column(name = "password")
    private String password;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user")

    private Set<UserOauth> userOauths = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user")
    private Set<UserRole> userRoles = new LinkedHashSet<>();

    public static User createByAdmin(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setActive(true);
        user.setVerifiedAt(LocalDateTime.now());
        return user;
    }
}
