package com.fernirx.sneakerapi.customer.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.customer.enums.PointTransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "point_transactions", indexes = {
        @Index(name = "idx_point_transactions_customer", columnList = "customer_id"),
        @Index(name = "idx_point_transactions_reference", columnList = "reference_id")
})
public class PointTransaction extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Long amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PointTransactionType type;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
