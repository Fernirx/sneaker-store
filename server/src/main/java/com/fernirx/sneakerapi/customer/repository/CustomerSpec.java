package com.fernirx.sneakerapi.customer.repository;

import com.fernirx.sneakerapi.customer.dto.request.CustomerFilterRequest;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CustomerSpec {

    public static Specification<Customer> build(CustomerFilterRequest filter) {
        return Specification
                .where(hasMembershipTier(filter.membershipTier()))
                .and(hasKeyword(filter.search()));
    }

    private static Specification<Customer> hasMembershipTier(MembershipTier tier) {
        return (root, query, cb) -> tier == null ? null
                : cb.equal(root.get("membershipTier"), tier);
    }

    private static Specification<Customer> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            Join<Customer, User> user = root.join("user", JoinType.LEFT);
            Join<User, UserProfile> profile = user.join("userProfile", JoinType.LEFT);
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(user.get("email")), pattern),
                    cb.like(cb.lower(profile.get("firstName")), pattern),
                    cb.like(cb.lower(profile.get("lastName")), pattern)
            );
        };
    }
}
