package com.fernirx.sneakerapi.banner.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "banners", indexes = {@Index(name = "idx_banners_active_order",
        columnList = "active, display_order")})
public class Banner extends BaseAuditEntity {
    @Size(max = 150)
    @NotNull
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Size(max = 255)
    @NotNull
    @Column(name = "image_public_id", nullable = false)
    private String imagePublicId;

    @Size(max = 500)
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;
}
