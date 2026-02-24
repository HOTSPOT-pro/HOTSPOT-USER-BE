package hotspot.user.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.dispatch.registry.SseEmitterRegistry;
import hotspot.user.dispatch.service.SubscribeSseServiceImpl;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class SubscribeSseServiceImplTest {

    @Mock
    private SseEmitterRegistry sseEmitterRegistry;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscribeSseServiceImpl subscribeSseService;

    @Test
    @DisplayName("subscribe registers emitter and sends connected event")
    void subscribeSuccess() {
        Subscription subscription = Subscription.builder().id(11L).build();
        given(subscriptionService.findByMemberId(1L)).willReturn(subscription);
        given(sseEmitterRegistry.register(org.mockito.ArgumentMatchers.eq(11L), any(SseEmitter.class)))
                .willReturn("e-1");

        SseEmitter result = subscribeSseService.subscribe(1L, "100");

        assertThat(result).isNotNull();
        then(subscriptionService).should().findByMemberId(1L);
        then(sseEmitterRegistry).should().register(org.mockito.ArgumentMatchers.eq(11L), any(SseEmitter.class));
        then(sseEmitterRegistry).should().sendAndCleanupOnFailure(any(), any());
    }

    @Test
    @DisplayName("sendHeartbeat sends heartbeat to all registered emitters")
    void sendHeartbeatSuccess() {
        List<SseEmitterRegistry.RegisteredEmitter> emitters = List.of(
                new SseEmitterRegistry.RegisteredEmitter("e-1", new SseEmitter()),
                new SseEmitterRegistry.RegisteredEmitter("e-2", new SseEmitter())
        );
        given(sseEmitterRegistry.findAll()).willReturn(emitters);

        subscribeSseService.sendHeartbeat();

        then(sseEmitterRegistry).should().findAll();
        then(sseEmitterRegistry).should(times(2)).sendAndCleanupOnFailure(any(), any());
    }

    @Test
    @DisplayName("subscribe throws NOTIFICATION_NOT_FOUND when subscription is missing")
    void subscribeFailWhenSubscriptionNotFound() {
        given(subscriptionService.findByMemberId(1L))
                .willThrow(new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        assertThatThrownBy(() -> subscribeSseService.subscribe(1L, null))
                .isInstanceOf(ApplicationException.class)
                .extracting(ex -> ((ApplicationException) ex).getCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
