package hotspot.user.usage.totalUsage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;
import hotspot.user.usage.totalUsage.service.port.TotalUsageRepository;

@ExtendWith(MockitoExtension.class)
class FindTotalUsageServiceImplTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private TotalUsageRepository totalUsageRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private FindTotalUsageServiceImpl service;

    @Test
    @DisplayName("전체 데이터 사용량 조회 성공")
    void shouldReturnTotalUsageResponse() {

        Instant fixedInstant = Instant.parse("2026-03-10T10:00:00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Plan plan =
                Plan.builder()
                        .name("5G 프리미엄")
                        .dataPeriod(DataPeriod.MONTH)
                        .build();

        Subscription subscription =
                Subscription.builder()
                        .id(7L)
                        .plan(plan)
                        .build();

        when(subscriptionService.findByMemberId(1L))
                .thenReturn(subscription);

        when(familySubscriptionRepository.findFamilyIdByMemberId(1L))
                .thenReturn(Optional.of(2L));

        TotalUsage usage =
                new TotalUsage(
                        38.0,
                        31.0,
                        82,
                        17.0,
                        6.0,
                        8.0
                );

        when(totalUsageRepository.findTotalUsage(7L, 2L, DataPeriod.MONTH))
                .thenReturn(usage);

        TotalUsageResponse result =
                service.findTotalUsage(1L);

        assertEquals(7L, result.subId());
        assertEquals("5G 프리미엄", result.planName());
        assertEquals(38.0, result.totalDataAmount());
        assertEquals(31.0, result.totalDataRemainAmount());
        assertEquals(82, result.totalDataRemainPercent());
        assertEquals(17.0, result.subDataRemainAmount());
        assertEquals(6.0, result.giftDataRemainAmount());
        assertEquals(8.0, result.familyDataRemainAmount());

        assertEquals(
                LocalDateTime.ofInstant(fixedInstant, ZoneId.systemDefault()),
                result.currentTime()
        );
    }
}
