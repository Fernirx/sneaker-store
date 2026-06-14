package com.fernirx.sneakerapi.category.repository;

import com.fernirx.sneakerapi.category.entity.CategoryTranslation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CategoryTranslation ct WHERE ct.category.id = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
}
