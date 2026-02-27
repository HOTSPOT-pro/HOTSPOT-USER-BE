package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.constant.FamilyConstant;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

/**
 * 구성원의 데이터 한도 조정하는 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class UpdateDataLimitServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private UpdateDataLimitServiceImpl updateDataLimitService;

    @Test
    @DisplayName("성공: OWNER가 동일 가족 구성원의 데이터 한도를 업데이트한다 (GB -> KB 변환 확인)")
    void updateDataLimitSuccess() {
        // given
        Long familyId = 1L;
        Long subId = 100L;
        long newDataLimitGb = 5L;
        long expectedKb = 5L * 1024L * 1024L;
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(familyId, subId, newDataLimitGb, false);

        FamilySubscription familySub = createFamilySubscription(familyId, subId, 1000);
        FamilySubDataLimit finalState = FamilySubDataLimit.builder()
                .familyId(familyId)
                .name("김태연")
                .isLocked(false)
                .dataLimit(expectedKb)
                .build();

        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));
        given(familySubscriptionRepository.findDataLimitBySubId(subId)).willReturn(finalState);

        // when
        UpdateDataLimitResponse response = updateDataLimitService.updateDataLimit(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(response.subId()).isEqualTo(subId);
        verify(familySubscriptionRepository, times(1)).updateDataLimit(eq(subId), eq(expectedKb));
        verify(subscriptionRepository, times(1)).updateLockedStatus(eq(subId), eq(false));
    }

    @Test
    @DisplayName("성공: 데이터 한도를 0으로 설정하면 즉시 차단(isLocked=true)된다")
    void updateDataLimitToZeroAndLock() {
        // given
        Long familyId = 1L;
        Long subId = 100L;
        long newDataLimitGb = 0L;
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(familyId, subId, newDataLimitGb, true);

        FamilySubscription familySub = createFamilySubscription(familyId, subId, 1000);
        FamilySubDataLimit finalState = FamilySubDataLimit.builder()
                .familyId(familyId)
                .isLocked(true)
                .dataLimit(0L)
                .build();

        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));
        given(familySubscriptionRepository.findDataLimitBySubId(subId)).willReturn(finalState);

        // when
        updateDataLimitService.updateDataLimit(request, familyId, FamilyRole.OWNER);

        // then
        verify(familySubscriptionRepository, times(1)).updateDataLimit(eq(subId), eq(0L));
        verify(subscriptionRepository, times(1)).updateLockedStatus(eq(subId), eq(true));
    }

    @Test
    @DisplayName("실패: 요청자가 OWNER가 아니면 예외가 발생한다")
    void updateDataLimitFailNotOwner() {
        // given
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(1L, 100L, 5L, false);

        // when & then
        assertThatThrownBy(() -> updateDataLimitService.updateDataLimit(request, 1L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 다른 가족의 구성원 데이터를 수정하려 하면 예외가 발생한다")
    void updateDataLimitFailDifferentFamily() {
        // given
        Long requesterFamilyId = 1L;
        Long targetFamilyId = 2L;
        Long subId = 100L;
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(requesterFamilyId, subId, 5L, false);

        FamilySubscription familySub = createFamilySubscription(targetFamilyId, subId, 1000);
        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> updateDataLimitService.updateDataLimit(request, requesterFamilyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.NOT_FAMILY_MEMBER);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 subId로 요청하면 예외가 발생한다")
    void updateDataLimitFailNotFound() {
        // given
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(1L, 999L, 5L, false);
        given(familySubscriptionRepository.findBySubId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateDataLimitService.updateDataLimit(request, 1L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("성공: 데이터 한도를 무제한(-1)으로 업데이트할 수 있다")
    void updateDataLimitToUnlimited() {
        // given
        long unlimitedGb = (long) FamilyConstant.UNLIMITED_DATA_LIMIT;
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(1L, 100L, unlimitedGb, false);
        FamilySubscription familySub = createFamilySubscription(1L, 100L, 1000);
        FamilySubDataLimit finalState = FamilySubDataLimit.builder()
                .familyId(1L)
                .isLocked(false)
                .dataLimit(unlimitedGb)
                .build();

        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.of(familySub));
        given(familySubscriptionRepository.findDataLimitBySubId(100L)).willReturn(finalState);

        // when
        UpdateDataLimitResponse response = updateDataLimitService.updateDataLimit(request, 1L, FamilyRole.OWNER);

        // then
        assertThat(response.dataLimit()).isEqualTo(unlimitedGb);
        verify(familySubscriptionRepository, times(1)).updateDataLimit(eq(100L), eq(unlimitedGb));
    }

    @Test
    @DisplayName("실패: 데이터 한도를 무제한보다 작은 값으로 수정하려 하면 예외가 발생한다")
    void updateDataLimitFailInvalidValue() {
        // given
        long invalidLimit = (long) FamilyConstant.UNLIMITED_DATA_LIMIT - 1L;
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(1L, 100L, invalidLimit, false);
        FamilySubscription familySub = createFamilySubscription(1L, 100L, 1000);
        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> updateDataLimitService.updateDataLimit(request, 1L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.INVALID_DATA_LIMIT);
    }

    @Test
    @DisplayName("실패: 설정하려는 한도가 가족 전체 데이터 양을 초과하면 예외가 발생한다")
    void updateDataLimitFailExceedsFamilyAmount() {
        // given
        Long familyId = 1L;
        Long subId = 100L;
        long newDataLimitGb = 10L; // 10GB 요청
        int familyDataAmountKb = 5 * 1024 * 1024; // 가족 총량은 5GB
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(familyId, subId, newDataLimitGb, false);

        FamilySubscription familySub = createFamilySubscription(familyId, subId, 1000, familyDataAmountKb);
        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> updateDataLimitService.updateDataLimit(request, familyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.DATA_LIMIT_EXCEEDS_FAMILY_AMOUNT);
    }

    private FamilySubscription createFamilySubscription(Long familyId, Long subId, int currentLimit, int familyDataAmount) {
        return FamilySubscription.builder()
                .id(1L)
                .family(Family.builder()
                        .id(familyId)
                        .familyDataAmount(familyDataAmount)
                        .build())
                .subscription(Subscription.builder().id(subId).build())
                .dataLimit(currentLimit)
                .build();
    }

    private FamilySubscription createFamilySubscription(Long familyId, Long subId, int currentLimit) {
        return createFamilySubscription(familyId, subId, currentLimit, 100 * 1024 * 1024); // 기본 100GB
    }
}
