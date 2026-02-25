package hotspot.user.dispatch.service;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.dispatch.controller.port.SubscribeSseService;
import hotspot.user.dispatch.domain.SseConnectedPayload;
import hotspot.user.dispatch.registry.SseEmitterRegistry;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscribeSseServiceImpl implements SubscribeSseService {

    private static final long SSE_TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final SseEmitterRegistry sseEmitterRegistry;
    private final SubscriptionService subscriptionService;

    @Override
    // SSE 연결을 생성, 등록하고 콜백을 걸고 즉시 connected 이벤트를 보내고 emitter를 반환한다.
    public SseEmitter subscribe(Long memberId, String lastEventId) {
        Long subId = resolveSubIdByMemberId(memberId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        String emitterId = sseEmitterRegistry.register(subId, emitter);

        emitter.onCompletion(() -> sseEmitterRegistry.remove(emitterId));
        emitter.onTimeout(() -> sseEmitterRegistry.remove(emitterId));
        emitter.onError(ex -> sseEmitterRegistry.remove(emitterId));

        sseEmitterRegistry.sendAndCleanupOnFailure(
                new SseEmitterRegistry.RegisteredEmitter(emitterId, emitter),
                SseEmitter.event()
                        .name("connected")
                        .id(emitterId)
                        .data(SseConnectedPayload.builder()
                                .subId(subId)
                                .lastEventId(lastEventId)
                                .connectedTime(LocalDateTime.now())
                                .build())
        );

        return emitter;
    }

    @Scheduled(fixedDelayString = "${app.notification.sse.heartbeat-interval-ms:25000}")
    // 현재 등록된 모든 SSE 연결에 주기적으로 heartbeat 이벤트를 보내며 끊긴 연결은 즉시 정리한다.
    public void sendHeartbeat() {
        sseEmitterRegistry.findAll()
                .forEach(registeredEmitter -> sseEmitterRegistry.sendAndCleanupOnFailure(
                        registeredEmitter,
                        SseEmitter.event()
                                .name("heartbeat")
                                .data(LocalDateTime.now())
                ));
    }

    // 구독을 조회하고 없으면 알림 관련 예외로 변환해 처리한다.
    private Long resolveSubIdByMemberId(Long memberId) {
        try {
            Subscription subscription = subscriptionService.findByMemberId(memberId);
            return subscription.getId();
        } catch (ApplicationException e) {
            if (e.getCode() == SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND) {
                throw new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
            }
            throw e;
        }
    }
}
