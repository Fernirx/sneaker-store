package com.fernirx.sneakerapi.user.repository;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.user.dto.request.UserFilterRequest;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import com.fernirx.sneakerapi.user.entity.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpec {

    public static Specification<User> build(UserFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(hasRole(filter.role()))
                .and(isActive(filter.active()));
    }

    private static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            Join<User, UserProfile> profile = root.join("userProfile", JoinType.LEFT);
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(profile.get("firstName")), pattern),
                    cb.like(cb.lower(profile.get("lastName")), pattern)
            );
        };
    }

    private static Specification<User> hasRole(Role role) {
        return (root, query, cb) -> {
            if (role == null) return null;
            Join<User, UserRole> userRole = root.join("userRoles", JoinType.LEFT);
            return cb.equal(userRole.get("role"), role);
        };
    }

    private static Specification<User> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }
}
