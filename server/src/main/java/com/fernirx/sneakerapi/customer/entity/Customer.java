package com.fernirx.sneakerapi.customer.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "customers", uniqueConstraints = {@UniqueConstraint(name = "user_id_UNIQUE",
        columnNames = {"user_id"})})
public class Customer extends BaseAuditEntity {
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ColumnDefault("'0'")
    @Column(name = "loyalty_points", columnDefinition = "int UNSIGNED not null")
    private Long loyaltyPoints;

    @NotNull
    @ColumnDefault("0.00")
    @Column(name = "total_spent", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSpent;

    @NotNull
    @ColumnDefault("'BRONZE'")
    @Enumerated(EnumType.STRING)
    @Lob
    @Column(name = "membership_tier", nullable = false)
    private MembershipTier membershipTier;

    @OneToMany(mappedBy = "customer")
    private Set<Address> addresses = new LinkedHashSet<>();
}