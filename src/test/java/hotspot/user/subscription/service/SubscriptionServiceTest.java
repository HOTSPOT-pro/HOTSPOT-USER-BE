package hotspot.user.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    SubscriptionRepository subscriptionRepository;

    @InjectMocks
    SubscriptionService subscriptionService;

    @Test
    @DisplayName("memberId로 Subscription 정상 조회")
    void shouldReturnSubscriptionSuccessfully() {

        // given
        Long memberId = 1L;

        Subscription subscription = Subscription.builder()
                .id(10L)
                .build();

        when(subscriptionRepository.findByMemberId(memberId))
                .thenReturn(Optional.of(subscription));

        // when
        Subscription result =
                subscriptionService.findByMemberId(memberId);

        // then
        assertEquals(subscription, result);
    }

    @Test
    @DisplayName("memberId에 해당하는 Subscription이 없으면 예외 발생")
    void shouldThrowExceptionWhenSubscriptionNotFound() {

        // given
        Long memberId = 1L;

        when(subscriptionRepository.findByMemberId(memberId))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(ApplicationException.class, () ->
                subscriptionService.findByMemberId(memberId)
        );
    }
}
