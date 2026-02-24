package hotspot.user.usage.subscriptionUsage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.domain.GiftUsage;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;

@ExtendWith(MockitoExtension.class)
class FindSubscriptionUsageServiceImplTest {

    @Mock
    SubscriptionUsageRepository subscriptionUsageRepository;

    @Mock
    PresentDataRepository presentDataRepository;

    @Mock
    Clock clock;

    @Mock
    SubscriptionService subscriptionService;

    @InjectMocks
    FindSubscriptionUsageServiceImpl service;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(
                LocalDateTime.of(2026, 2, 1, 0, 0)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    @DisplayName("개인 데이터 사용량 서비스 정상 동작")
    void shouldReturnSubscriptionUsageSuccessfully() {

        // given
        Long memberId = 100L;
        Long subscriptionId = 1L;

        Plan plan = Plan.builder()
                .dataPeriod(DataPeriod.MONTH)
                .build();

        Subscription subscription = Subscription.builder()
                .id(subscriptionId)
                .plan(plan)
                .build();

        SubscriptionUsage mockUsage =
                new SubscriptionUsage(
                        subscriptionId,
                        24 * 1024 * 1024, // 24GB KB 단위
                        0,
                        List.of(
                                new GiftUsage(
                                        69395L,
                                        1 * 1024 * 1024,
                                        512 * 1024 // 0.5GB
                                )
                        )
                );

        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(subscription);

        when(subscriptionUsageRepository.findSubscriptionUsage(
                eq(subscriptionId),
                eq(DataPeriod.MONTH)
        )).thenReturn(mockUsage);

        when(presentDataRepository.findGiftGiverNames(eq(List.of(69395L))))
                .thenReturn(Map.of(69395L, "김태연"));

        // when
        SubscriptionUsageResponse response =
                service.findSubscriptionUsage(memberId);

        // then
        assertNotNull(response);
        assertEquals(subscriptionId, response.subId());

        assertEquals(1, response.giftUsages().size());
        assertEquals("김태연",
                response.giftUsages().get(0).giftUserName());

        verify(subscriptionService)
                .findByMemberId(memberId);

        verify(subscriptionUsageRepository)
                .findSubscriptionUsage(
                        subscriptionId,
                        DataPeriod.MONTH
                );

        verify(presentDataRepository)
                .findGiftGiverNames(anyList());
    }
}
