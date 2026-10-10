package com.fernirx.sneakerapi.order.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "order_status_history", indexes = {
        @Index(name = "idx_status_history_order_created",
                columnList = "order_id, created_at"),
        @Index(name = "idx_status_history_changed_by",
                columnList = "changed_by")})
public class OrderStatusHistory extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private OrderStatus oldStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}