package com.fernirx.sneakerapi.collection.entity;

import com.fernirx.sneakerapi.common.entity.BaseAuditEntity;
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
@Table(name = "collection_translations", uniqueConstraints = {@UniqueConstraint(name = "collection_locale_UNIQUE",
        columnNames = {
                "collection_id",
                "locale"})})
public class CollectionTranslation extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Size(max = 10)
    @NotNull
    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}