package com.fernirx.sneakerapi.shipping.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.shipping.enums.ShipmentProvider;
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
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_order_code",
                columnList = "shipping_order_code")}, uniqueConstraints = {@UniqueConstraint(name = "order_id_UNIQUE",
        columnNames = {"order_id"})})
public class Shipment extends BaseAuditEntity {
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ColumnDefault("'GHN'")
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private ShipmentProvider provider = ShipmentProvider.GHN;

    @Size(max = 50)
    @Column(name = "shipping_order_code", length = 50)
    private String shippingOrderCode;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "expected_delivery_at")
    private LocalDateTime expectedDeliveryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
