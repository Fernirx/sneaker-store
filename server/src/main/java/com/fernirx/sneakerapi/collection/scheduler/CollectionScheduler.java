package com.fernirx.sneakerapi.collection.scheduler;

import com.fernirx.sneakerapi.collection.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CollectionScheduler {
    private final CollectionRepository collectionRepository;

    // Chạy mỗi ngày lúc 00:00 — tắt các collection đã hết endDate
    @Scheduled(cron = "0 0 0 * * *")
    public void deactivateExpiredCollections() {
        collectionRepository.deactivateExpiredCollections(LocalDate.now());
    }
}
