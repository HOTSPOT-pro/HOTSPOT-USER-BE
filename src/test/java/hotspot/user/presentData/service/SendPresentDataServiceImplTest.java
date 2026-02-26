package hotspot.user.presentData.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.gift.GiftReceivedEvent;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;

@ExtendWith(MockitoExtension.class)
class SendPresentDataServiceImplTest {

    private static final long ONE_GB_IN_KB = 1_048_576L;

    @Mock
    private PresentDataRepository presentDataRepository;

    @Mock
    private FindFamilySubscriptionService findFamilySubscriptionService;

    @Mock
    private SubscriptionUsageRepository subscriptionUsageRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Clock fixedClock;

    @InjectMocks
    private SendPresentDataServiceImpl sendPresentDataService;

    private Long memberId;
    private Long targetSubId;
    private Family family;
    private Subscription providerSub;
    private Subscription targetSub;
    private FamilySubscription providerFamilySub;
    private FamilySubscription targetFamilySub;

    @BeforeEach
    void setUp() {

        fixedClock = Clock.fixed(
                Instant.parse("2026-02-19T00:00:00Z"),
                ZoneId.systemDefault()
        );

        sendPresentDataService = new SendPresentDataServiceImpl(
                presentDataRepository,
                findFamilySubscriptionService,
                subscriptionUsageRepository,
                eventPublisher,
                fixedClock
        );

        memberId = 1L;
        targetSubId = 2L;

        family = Family.builder().id(10L).build();

        Plan plan = Plan.builder()
                .dataPeriod(DataPeriod.MONTH)
                .build();

        providerSub = Subscription.builder()
                .id(1L)
                .plan(plan)
                .build();

        targetSub = Subscription.builder()
                .id(2L)
                .plan(plan)
                .build();

        providerFamilySub = FamilySubscription.builder()
                .family(family)
                .subscription(providerSub)
                .build();

        targetFamilySub = FamilySubscription.builder()
                .family(family)
                .subscription(targetSub)
                .build();
    }

    @Test
    @DisplayName("데이터 선물하기 성공")
    void sendPresentDataSuccess() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 1L);

        PresentData savedPresentData = PresentData.builder()
                .provideSubscription(providerSub)
                .targetSubscription(targetSub)
                .dataAmount(ONE_GB_IN_KB)
                .build();

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(providerFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(targetFamilySub);

        // 잔여량 충분
        when(subscriptionUsageRepository.findRemainingPlanKb(anyLong(), any()))
                .thenReturn(10L * ONE_GB_IN_KB);

        // 이번달 선물한 총량 0
        when(presentDataRepository.sumMonthlySentKb(anyLong(), any(), any()))
                .thenReturn(0L);

        when(presentDataRepository.sendPresentData(any()))
                .thenReturn(savedPresentData);

        SendPresentDataResponse response =
                sendPresentDataService.sendPresentData(memberId, request);

        assertThat(response.provideSubId()).isEqualTo(providerSub.getId());
        assertThat(response.targetSubId()).isEqualTo(targetSub.getId());
        assertThat(response.dataAmount()).isEqualTo(ONE_GB_IN_KB);

        verify(eventPublisher, times(1)).publishEvent(isA(GiftReceivedEvent.class));
    }

    @Test
    @DisplayName("잔여 데이터 부족 시 예외")
    void notEnoughData() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 3L);

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(providerFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(targetFamilySub);

        when(subscriptionUsageRepository.findRemainingPlanKb(anyLong(), any()))
                .thenReturn(1L * ONE_GB_IN_KB); // 부족

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue(
                        "code",
                        PresentDataErrorCode.NOT_ENOUGH_DATA
                );
    }

    @Test
    @DisplayName("월 한도 초과 시 예외")
    void monthlyLimitExceeded() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 3L);

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(providerFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(targetFamilySub);

        when(subscriptionUsageRepository.findRemainingPlanKb(anyLong(), any()))
                .thenReturn(10L * ONE_GB_IN_KB);

        // 이미 4GB 사용한 상태
        when(presentDataRepository.sumMonthlySentKb(anyLong(), any(), any()))
                .thenReturn(4L * ONE_GB_IN_KB);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue(
                        "code",
                        PresentDataErrorCode.MONTHLY_GIFT_LIMIT_EXCEEDED
                );
    }

    @Test
    @DisplayName("같은 가족이 아닐 경우 예외")
    void notSameFamily() {

        Family otherFamily = Family.builder().id(99L).build();

        FamilySubscription otherFamilySub =
                FamilySubscription.builder()
                        .family(otherFamily)
                        .subscription(targetSub)
                        .build();

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 1L);

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(providerFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(otherFamilySub);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue(
                        "code",
                        AuthErrorCode.ACCESS_DENIED
                );
    }
}
