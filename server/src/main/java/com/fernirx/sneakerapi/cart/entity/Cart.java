package com.fernirx.sneakerapi.cart.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.customer.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "carts", uniqueConstraints = {
        @UniqueConstraint(name = "cart_customer_UNIQUE",
                columnNames = {"customer_id"}),
        @UniqueConstraint(name = "cart_guest_UNIQUE",
                columnNames = {"guest_token"})})
public class Cart extends BaseAuditEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Size(max = 64)
    @Column(name = "guest_token", length = 64)
    private String guestToken;

    @OneToMany(mappedBy = "cart", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<CartItem> cartItems = new LinkedHashSet<>();
}