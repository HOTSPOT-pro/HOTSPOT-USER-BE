package hotspot.user.presentData.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class SendPresentDataServiceImplTest {

    @Mock
    private PresentDataRepository presentDataRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @InjectMocks
    private SendPresentDataServiceImpl sendPresentDataService;

    private Long memberId;
    private Long targetSubId;
    private Long oneGb;
    private Family family;
    private Subscription providerSub;
    private Subscription targetSub;
    private FamilySubscription providerFamilySub;
    private FamilySubscription targetFamilySub;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        targetSubId = 2L;
        oneGb = 1048576L;

        family = Family.builder()
                .id(10L)
                .build();

        providerSub = Subscription.builder()
                .id(1L)
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
    @DisplayName("데이터 선물하기 성공")
    void sendPresentDataSuccess() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, oneGb);
        PresentData presentData = PresentData.builder()
                .provideSubscription(providerSub)
                .targetSubscription(targetSub)
                .dataAmount(oneGb)
                .build();

        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.of(targetFamilySub));
        when(presentDataRepository.sendPresentData(any(PresentData.class))).thenReturn(presentData);

        // when
        SendPresentDataResponse response = sendPresentDataService.sendPresentData(memberId, request);

        // then
        assertThat(response.provideSubId()).isEqualTo(providerSub.getId());
        assertThat(response.targetSubId()).isEqualTo(targetSub.getId());
        assertThat(response.dataAmount()).isEqualTo(oneGb);
    }

    @Test
    @DisplayName("보내는 사람의 가족 결합 정보를 찾을 수 없을 때 예외 발생")
    void sendPresentDataProviderFamilyNotFound() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, oneGb);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("받는 사람의 가족 결합 정보를 찾을 수 없을 때 예외 발생")
    void sendPresentDataTargetFamilyNotFound() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, oneGb);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.of(targetFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
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

        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, oneGb);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.of(otherFamilySub));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("자신에게 선물할 때 예외 발생")
    void sendPresentDataSelfGift() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(providerSub.getId(), oneGb);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(providerSub.getId())).thenReturn(Optional.of(providerFamilySub));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_SELF_GIFT);
    }

    @Test
    @DisplayName("데이터 선물 요청량이 1GB 미만일 때 예외 발생")
    void sendPresentDataInvalidAmountTooSmall() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, 500L);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.of(targetFamilySub));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }

    @Test
    @DisplayName("데이터 선물 요청량이 5GB 초과일 때 예외 발생")
    void sendPresentDataInvalidAmountTooLarge() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, oneGb * 6);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.of(targetFamilySub));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }

    @Test
    @DisplayName("데이터 선물 요청량이 1GB 단위가 아닐 때 예외 발생")
    void sendPresentDataInvalidAmountNotStep() {
        // given
        SendPresentDataRequest request = new SendPresentDataRequest(targetSubId, oneGb + 100L);
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(providerFamilySub));
        when(familySubscriptionRepository.findBySubId(targetSubId)).thenReturn(Optional.of(targetFamilySub));

        // when & then
        assertThatThrownBy(() -> sendPresentDataService.sendPresentData(memberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
    }
}
