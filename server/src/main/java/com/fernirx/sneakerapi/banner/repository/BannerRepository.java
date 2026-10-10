package com.fernirx.sneakerapi.banner.repository;

import com.fernirx.sneakerapi.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long>, JpaSpecificationExecutor<Banner> {

    @Query("SELECT b FROM Banner b WHERE b.active = true " +
            "AND (b.startAt IS NULL OR b.startAt <= :now) " +
            "AND (b.endAt IS NULL OR b.endAt >= :now) " +
            "ORDER BY b.displayOrder ASC")
    List<Banner> findPublicActive(@Param("now") LocalDateTime now);
}
