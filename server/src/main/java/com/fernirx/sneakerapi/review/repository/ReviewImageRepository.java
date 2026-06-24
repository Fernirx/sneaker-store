package com.fernirx.sneakerapi.review.repository;

import com.fernirx.sneakerapi.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findByReview_IdInOrderByReview_IdAscDisplayOrderAsc(List<Long> reviewIds);

    void deleteByReview_Id(Long reviewId);
}
