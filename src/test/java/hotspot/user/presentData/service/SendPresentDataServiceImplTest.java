package hotspot.user.presentData.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.kafka.outbox.NotificationUserAlertOutboxPublisher;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class SendPresentDataServiceImplTest {

    private static final long ONE_GB_IN_KB = 1048576L;

    @Mock
    private PresentDataRepository presentDataRepository;

    @Mock
    private FindFamilySubscriptionService findFamilySubscriptionService;

    @Mock
    private NotificationUserAlertOutboxPublisher userAlertOutboxPublisher;

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
        memberId = 1L;
        targetSubId = 2L;

        family = Family.builder()
                .id(10L)
                .build();

        providerSub = Subscription.builder()
                .id(1L)
                .member(Member.builder()
                        .id(memberId)
                        .name("Alice")
                        .birth("1990-01-01")
                        .status(Status.APPROVED)
                        .build())
                .build();

        targetSub = Subscription.builder()
                .id(2L)
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
    @DisplayName("데이터 선물하기 성공 (1GB 요청 시 내부적으로 KB 변환 확인)")
    void sendPresentDataSuccess() {
        // given
        Long requestAmountGb = 1L; // 프론트에서 보내는 값 (GB)
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, requestAmountGb);

        PresentData savedPresentData = PresentData.builder()
                .presentDataId(99L)
                .provideSubscription(providerSub)
                .targetSubscription(targetSub)
                .dataAmount(ONE_GB_IN_KB) // 저장된 값 (KB)
                .build();

        when(findFamilySubscriptionService.findByMemberId(memberId)).thenReturn(providerFamilySub);
        when(findFamilySubscriptionService.findBySubId(targetSubId)).thenReturn(targetFamilySub);
        when(presentDataRepository.sendPresentData(any(PresentData.class))).thenReturn(savedPresentData);

        // when
        SendPresentDataResponse response = sendPresentDataService.sendPresentData(memberId, request);

        // then
        assertThat(response.provideSubId()).isEqualTo(providerSub.getId());
        assertThat(response.targetSubId()).isEqualTo(targetSub.getId());
        assertThat(response.dataAmount()).isEqualTo(ONE_GB_IN_KB);
        verify(userAlertOutboxPublisher).publishPresentDataGifted(
                targetSubId,
                family.getId(),
                "Alice",
                "1GB",
                "99"
        );
    }

    @Test
    @DisplayName("보내는 사람의 가족 결합 정보를 찾을 수 없을 때 예외 발생")
    void sendPresentDataProviderFamilyNotFound() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, 1L);
        when(findFamilySubscriptionService.findByMemberId(memberId))
                .thenThrow(new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("받는 사람의 가족 결합 정보를 찾을 수 없을 때 예외 발생")
    void sendPresentDataTargetFamilyNotFound() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, 1L);
        when(findFamilySubscriptionService.findByMemberId(memberId)).thenReturn(providerFamilySub);
        when(findFamilySubscriptionService.findBySubId(targetSubId))
                .thenThrow(new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("같은 가족 구성원이 아닐 때 예외 발생")
    void sendPresentDataNotSameFamily() {
        // given
        Family otherFamily = Family.builder().id(20L).build();
        FamilySubscription otherFamilySub = FamilySubscription.builder()
                .family(otherFamily)
                .subscription(targetSub)
                .build();

        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, 1L);
        when(findFamilySubscriptionService.findByMemberId(memberId)).thenReturn(providerFamilySub);
        when(findFamilySubscriptionService.findBySubId(targetSubId)).thenReturn(otherFamilySub);

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("자신에게 선물할 때 예외 발생")
    void sendPresentDataSelfGift() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(providerSub.getId(), 1L);
        when(findFamilySubscriptionService.findByMemberId(memberId)).thenReturn(providerFamilySub);
        when(findFamilySubscriptionService.findBySubId(providerSub.getId())).thenReturn(providerFamilySub);

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_SELF_GIFT);
    }

    @Test
    @DisplayName("데이터 선물 요청량이 1GB 미만일 때 예외 발생")
    void sendPresentDataInvalidAmountTooSmall() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, 0L);
        when(findFamilySubscriptionService.findByMemberId(memberId)).thenReturn(providerFamilySub);
        when(findFamilySubscriptionService.findBySubId(targetSubId)).thenReturn(targetFamilySub);

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }

    @Test
    @DisplayName("데이터 선물 요청량이 5GB 초과일 때 예외 발생")
    void sendPresentDataInvalidAmountTooLarge() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, 6L);
        when(findFamilySubscriptionService.findByMemberId(memberId)).thenReturn(providerFamilySub);
        when(findFamilySubscriptionService.findBySubId(targetSubId)).thenReturn(targetFamilySub);

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }
}
