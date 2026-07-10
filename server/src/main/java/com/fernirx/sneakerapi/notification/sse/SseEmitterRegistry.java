package com.fernirx.sneakerapi.notification.sse;

import com.fernirx.sneakerapi.notification.dto.response.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Đăng ký kết nối SSE đang mở theo userId (in-memory - chỉ đúng khi chạy 1 instance,
 * xem ghi chú khả năng mở rộng ở DECISIONS_LOG). Dùng chung cho cả nhân viên lẫn khách hàng
 * vì cả 2 đều là userId trong bảng users.
 */
@Slf4j
@Component
public class SseEmitterRegistry {
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emittersByUserId.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(ex -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").comment("ok"));
        } catch (IOException ex) {
            remove(userId, emitter);
        }
        return emitter;
    }

    public void sendToUsers(List<Long> userIds, NotificationResponse payload) {
        for (Long userId : userIds) {
            CopyOnWriteArrayList<SseEmitter> emitters = emittersByUserId.get(userId);
            if (emitters == null) continue;
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("notification").data(payload));
                } catch (IOException ex) {
                    remove(userId, emitter);
                }
            }
        }
    }

    @Scheduled(fixedRate = 20_000)
    public void heartbeat() {
        emittersByUserId.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException ex) {
                    remove(userId, emitter);
                }
            }
        });
    }

    private void remove(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUserId.get(userId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUserId.remove(userId, emitters);
        }
    }
}
