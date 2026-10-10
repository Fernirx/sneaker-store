package com.fernirx.sneakerapi.user.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.customer.entity.Customer;
import com.fernirx.sneakerapi.inventory.entity.InventoryTransaction;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderStatusHistory;
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

    // cascade REMOVE: khi soft-delete User (xem @SQLDelete), Hibernate cascade sang Customer.remove() -
    // Customer tự có @SQLDelete riêng nên đây cũng là soft-delete (UPDATE deleted_at), không xóa cứng.
    // Đảm bảo 2 lifecycle User/Customer luôn đồng bộ trong cùng 1 transaction, tránh Customer "mồ côi"
    // vẫn hiển thị bình thường trong khi User đã bị ẩn.
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Customer customer;

    @OneToMany(mappedBy = "createdBy")
    private Set<InventoryTransaction> inventoryTransactions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "changedBy")
    private Set<OrderStatusHistory> orderStatusHistories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "assignedTo")
    private Set<Order> orders = new LinkedHashSet<>();

    public static User createByAdmin(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setActive(true);
        user.setVerifiedAt(LocalDateTime.now());
        return user;
    }
}
