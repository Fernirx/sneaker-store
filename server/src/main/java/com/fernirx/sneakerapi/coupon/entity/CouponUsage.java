package com.fernirx.sneakerapi.coupon.entity;

import com.fernirx.sneakerapi.common.entity.BaseEntity;
import com.fernirx.sneakerapi.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "coupon_usages", indexes = {
        @Index(name = "idx_coupon_usages_coupon_email",
                columnList = "coupon_id, email"),
        @Index(name = "idx_coupon_usages_coupon_phone",
                columnList = "coupon_id, phone")}, uniqueConstraints = {@UniqueConstraint(name = "order_id_UNIQUE",
        columnNames = {"order_id"})})
public class CouponUsage extends BaseEntity {
    // Không CASCADE - coupon đã có lịch sử sử dụng là dữ liệu đối soát tài chính, không được xóa âm thầm
    // theo Coupon. CouponServiceImpl.delete() tự chặn ở tầng service trước; RESTRICT ở đây là lớp bảo vệ
    // thứ 2 tại DB.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}