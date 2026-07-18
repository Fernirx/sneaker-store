package com.fernirx.sneakerapi.returns.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "return_request_images", indexes = {@Index(name = "idx_return_images_request",
        columnList = "return_request_id")}, uniqueConstraints = {@UniqueConstraint(name = "image_public_id_UNIQUE",
        columnNames = {"image_public_id"})})
public class ReturnRequestImage extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Size(max = 255)
    @NotNull
    @Column(name = "image_public_id", nullable = false)
    private String imagePublicId;
}
