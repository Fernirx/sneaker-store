package com.fernirx.sneakerapi.returns.repository;

import com.fernirx.sneakerapi.returns.entity.ReturnRequest;
import com.fernirx.sneakerapi.returns.entity.ReturnRequestImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestImageRepository extends JpaRepository<ReturnRequestImage, Long> {

    List<ReturnRequestImage> findAllByReturnRequest(ReturnRequest returnRequest);
}
