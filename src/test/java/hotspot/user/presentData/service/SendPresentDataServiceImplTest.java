package hotspot.user.presentData.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.gift.GiftReceivedEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.PresentDataGiftedOutboxEvent;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;

class SendPresentDataServiceImplTest {

    private static final long ONE_GB_IN_KB = 1_048_576L;

    private PresentDataRepository presentDataRepository;
    private FindFamilySubscriptionService findFamilySubscriptionService;
    private SubscriptionUsageRepository subscriptionUsageRepository;
    private UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;
    private ApplicationEventPublisher eventPublisher;

    private SendPresentDataServiceImpl sendPresentDataService;

    private Long memberId;
    private Long targetSubId;

    private Family family;
    private Plan plan;
    private Subscription giverSub;
    private Subscription receiverSub;
    private FamilySubscription giverFamilySub;
    private FamilySubscription receiverFamilySub;

    @BeforeEach
    void setUp() {

        presentDataRepository = mock(PresentDataRepository.class);
        findFamilySubscriptionService = mock(FindFamilySubscriptionService.class);
        subscriptionUsageRepository = mock(SubscriptionUsageRepository.class);
        userAlertNotificationOutboxPort = mock(UserAlertNotificationOutboxPort.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-01-01T00:00:00Z"),
                ZoneId.systemDefault()
        );

        sendPresentDataService = new SendPresentDataServiceImpl(
                presentDataRepository,
                findFamilySubscriptionService,
                subscriptionUsageRepository,
                eventPublisher,
                fixedClock,
                userAlertNotificationOutboxPort
        );

        memberId = 1L;
        targetSubId = 2L;

        family = Family.builder().id(10L).build();

        plan = Plan.builder()
                .id(1L)
                .dataPeriod(DataPeriod.MONTH)
                .dataAmount(10L)
                .build();

        giverSub = createSubscription(1L, memberId);
        receiverSub = createSubscription(2L, 999L);

        giverFamilySub = FamilySubscription.builder()
                .family(family)
                .subscription(giverSub)
                .build();

        receiverFamilySub = FamilySubscription.builder()
                .family(family)
                .subscription(receiverSub)
                .build();
    }

    private Subscription createSubscription(Long subId, Long memberId) {
        return Subscription.builder()
                .id(subId)
                .member(Member.builder()
                        .id(memberId)
                        .name("Alice")
                        .birth("1990-01-01")
                        .status(Status.APPROVED)
                        .build())
                .plan(plan)
                .build();
    }

    @Test
    @DisplayName("데이터 선물 성공 - 이벤트 payload 정확 검증")
    void sendPresentDataSuccess() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 1L);

        PresentData saved = PresentData.builder()
                .presentDataId(99L)
                .provideSubscription(giverSub)
                .targetSubscription(receiverSub)
                .dataAmount(ONE_GB_IN_KB)
                .build();

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(giverFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(receiverFamilySub);

        when(subscriptionUsageRepository.findRemainingPlanKb(anyLong(), any()))
                .thenReturn(10_000_000L);

        when(presentDataRepository.sumMonthlySentKb(anyLong(), any(), any()))
                .thenReturn(0L);

        when(presentDataRepository.sendPresentData(any()))
                .thenReturn(saved);

        // ===== 실행 =====
        SendPresentDataResponse response =
                sendPresentDataService.sendPresentData(memberId, request);

        // ===== 기본 응답 검증 =====
        assertThat(response.provideSubId()).isEqualTo(1L);
        assertThat(response.targetSubId()).isEqualTo(2L);
        assertThat(response.dataAmount()).isEqualTo(1L);

        // ===== 알림 Publisher 검증 =====
        ArgumentCaptor<PresentDataGiftedOutboxEvent> alertEventCaptor =
                ArgumentCaptor.forClass(PresentDataGiftedOutboxEvent.class);
        verify(userAlertNotificationOutboxPort).appendPresentDataGiftedAlert(alertEventCaptor.capture());
        PresentDataGiftedOutboxEvent alertEvent = alertEventCaptor.getValue();
        assertThat(alertEvent.targetSubId()).isEqualTo(targetSubId);
        assertThat(alertEvent.familyId()).isEqualTo(family.getId());
        assertThat(alertEvent.senderName()).isEqualTo("Alice");
        assertThat(alertEvent.presentAmount()).isEqualTo("1GB");
        assertThat(alertEvent.giftId()).isEqualTo("99");

        // ===== 이벤트 payload 정확 검증 =====
        ArgumentCaptor<GiftReceivedEvent> captor =
                ArgumentCaptor.forClass(GiftReceivedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        GiftReceivedEvent event = captor.getValue();

        assertThat(event.type()).isEqualTo("GIFT_RECEIVED");
        assertThat(event.receiverSubId()).isEqualTo(2L);
        assertThat(event.giverSubId()).isEqualTo(1L);
        assertThat(event.giftId()).isEqualTo(99L);
        assertThat(event.giftLimitBytes()).isEqualTo(ONE_GB_IN_KB);
        assertThat(event.giftAmountBytes()).isEqualTo(ONE_GB_IN_KB);
        assertThat(event.yyyyMM()).isEqualTo("202501");
        assertThat(event.yyyyMMDD()).isEqualTo("20250101");
        assertThat(event.eventId()).isNotNull();
    }

    // ===============================
    // amount 검증
    // ===============================

    @Test
    void invalidAmountTooSmall() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 0L);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code",
                        PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }

    @Test
    void invalidAmountTooLarge() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 6L);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code",
                        PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }

    // ===============================
    // 다른 가족
    // ===============================

    @Test
    void notSameFamily() {

        Family otherFamily = Family.builder().id(999L).build();

        FamilySubscription otherFamilySub = FamilySubscription.builder()
                .family(otherFamily)
                .subscription(receiverSub)
                .build();

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 1L);

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(giverFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(otherFamilySub);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code",
                        AuthErrorCode.ACCESS_DENIED);
    }

    // ===============================
    // 잔여 데이터 부족
    // ===============================

    @Test
    void notEnoughRemainingData() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 1L);

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(giverFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(receiverFamilySub);

        when(subscriptionUsageRepository.findRemainingPlanKb(anyLong(), any()))
                .thenReturn(100L);

        when(presentDataRepository.sumMonthlySentKb(anyLong(), any(), any()))
                .thenReturn(0L);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code",
                        PresentDataErrorCode.NOT_ENOUGH_DATA);
    }

    // ===============================
    // 월간 한도 초과
    // ===============================

    @Test
    void monthlyLimitExceeded() {

        SendPresentDataRequest request =
                new SendPresentDataRequest(targetSubId, 5L);

        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenReturn(giverFamilySub);

        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenReturn(receiverFamilySub);

        when(subscriptionUsageRepository.findRemainingPlanKb(anyLong(), any()))
                .thenReturn(10_000_000L);

        when(presentDataRepository.sumMonthlySentKb(anyLong(), any(), any()))
                .thenReturn(ONE_GB_IN_KB);

        assertThatThrownBy(() ->
                sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code",
                        PresentDataErrorCode.MONTHLY_GIFT_LIMIT_EXCEEDED);
    }
}
