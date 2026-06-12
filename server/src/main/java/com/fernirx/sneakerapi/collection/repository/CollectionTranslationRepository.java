package com.fernirx.sneakerapi.collection.repository;

import com.fernirx.sneakerapi.collection.entity.CollectionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CollectionTranslationRepository extends JpaRepository<CollectionTranslation, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CollectionTranslation ct WHERE ct.collection.id = :collectionId")
    void deleteByCollectionId(@Param("collectionId") Long collectionId);
}
