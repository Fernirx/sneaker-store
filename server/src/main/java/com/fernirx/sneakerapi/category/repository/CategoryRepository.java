package com.fernirx.sneakerapi.category.repository;

import com.fernirx.sneakerapi.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
}
