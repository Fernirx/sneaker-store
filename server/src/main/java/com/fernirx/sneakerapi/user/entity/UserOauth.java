package com.fernirx.sneakerapi.user.entity;

import com.fernirx.sneakerapi.common.entity.BaseCreatedEntity;
import com.fernirx.sneakerapi.user.enums.Provider;
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
@Table(name = "user_oauth", indexes = {@Index(name = "idx_oauth_user",
        columnList = "user_id")}, uniqueConstraints = {@UniqueConstraint(name = "provider_id_UNIQUE",
        columnNames = {
                "provider",
                "provider_id"})})
public class UserOauth extends BaseCreatedEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Size(max = 255)
    @NotNull
    @Column(name = "provider_id", nullable = false)
    private String providerId;
}
