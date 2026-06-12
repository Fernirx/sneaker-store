package com.fernirx.sneakerapi.collection.repository;

import com.fernirx.sneakerapi.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long>, JpaSpecificationExecutor<Collection> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsBySlug(String slug);
    Optional<Collection> findBySlug(String slug);

    @Modifying
    @Transactional
    @Query("UPDATE Collection c SET c.active = false WHERE c.endDate < :today AND c.active = true")
    void deactivateExpiredCollections(@Param("today") LocalDate today);
}
