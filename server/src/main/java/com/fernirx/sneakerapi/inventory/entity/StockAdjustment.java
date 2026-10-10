package com.fernirx.sneakerapi.inventory.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentType;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "stock_adjustments", indexes = {@Index(name = "idx_adjustments_type_status",
        columnList = "type, status")}, uniqueConstraints = {@UniqueConstraint(name = "code_UNIQUE",
        columnNames = {"code"})})
public class StockAdjustment extends BaseAuditEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StockAdjustmentType type;

    @NotNull
    @ColumnDefault("'DRAFT'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StockAdjustmentStatus status;

    @NotNull
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "adjustment")
    private Set<StockAdjustmentItem> stockAdjustmentItems = new LinkedHashSet<>();
}
