package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import hotspot.user.outbox.consistencyOutbox.domain.event.family.limit.FamilySubLimitChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 구성원의 데이터 한도 조정하는 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class UpdateDataLimitServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateDataLimitServiceImpl updateDataLimitService;

    private static final long GB_TO_KB_UNIT = 1_048_576L;

    @Test
    @DisplayName("성공: OWNER가 동일 가족 구성원의 데이터 한도를 업데이트한다")
    void updateDataLimitSuccess() {

        Long familyId = 1L;
        Long subId = 100L;
        int newDataLimitGb = 5;  // 5GB

        UpdateDataLimitRequest request =
                new UpdateDataLimitRequest(familyId, subId, newDataLimitGb);

        FamilySubscription familySub =
                createFamilySubscription(familyId, subId, 1);

        given(familySubscriptionRepository.findBySubId(subId))
                .willReturn(Optional.of(familySub));

        given(familySubscriptionRepository.save(any(FamilySubscription.class)))
                .willReturn(familySub);

        // when
        UpdateDataLimitResponse response =
                updateDataLimitService.updateDataLimit(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(response.subId()).isEqualTo(subId);
        assertThat(response.dataLimit()).isEqualTo(newDataLimitGb);

        verify(familySubscriptionRepository, times(1))
                .save(any(FamilySubscription.class));

        ArgumentCaptor<FamilySubLimitChangedEvent> captor =
                ArgumentCaptor.forClass(FamilySubLimitChangedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        FamilySubLimitChangedEvent event = captor.getValue();

        assertThat(event.familyId()).isEqualTo(familyId);
        assertThat(event.subId()).isEqualTo(subId);
        
        long expectedKb = newDataLimitGb * GB_TO_KB_UNIT;
        assertThat(event.newLimit()).isEqualTo(expectedKb);
    }

    @Test
    @DisplayName("실패: 요청자가 OWNER가 아니면 예외가 발생한다")
    void updateDataLimitFailNotOwner() {

        UpdateDataLimitRequest request =
                new UpdateDataLimitRequest(1L, 100L, 5);

        assertThatThrownBy(() ->
                updateDataLimitService.updateDataLimit(request, 1L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());

        verify(eventPublisher, times(0)).publishEvent(any());
    }

    @Test
    @DisplayName("실패: 다른 가족의 구성원 데이터를 수정하려 하면 예외가 발생한다")
    void updateDataLimitFailDifferentFamily() {

        Long requesterFamilyId = 1L;
        Long targetFamilyId = 2L;
        Long subId = 100L;

        UpdateDataLimitRequest request =
                new UpdateDataLimitRequest(requesterFamilyId, subId, 5);

        FamilySubscription familySub =
                createFamilySubscription(targetFamilyId, subId, 1);

        given(familySubscriptionRepository.findBySubId(subId))
                .willReturn(Optional.of(familySub));

        assertThatThrownBy(() ->
                updateDataLimitService.updateDataLimit(request, requesterFamilyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());

        verify(eventPublisher, times(0)).publishEvent(any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 subId로 요청하면 예외가 발생한다")
    void updateDataLimitFailNotFound() {

        UpdateDataLimitRequest request =
                new UpdateDataLimitRequest(1L, 999L, 5);

        given(familySubscriptionRepository.findBySubId(999L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                updateDataLimitService.updateDataLimit(request, 1L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND.getMessage());

        verify(eventPublisher, times(0)).publishEvent(any());
    }

    private FamilySubscription createFamilySubscription(
            Long familyId,
            Long subId,
            int currentLimit
    ) {
        return FamilySubscription.builder()
                .id(1L)
                .family(Family.builder().id(familyId).build())
                .subscription(Subscription.builder().id(subId).build())
                .dataLimit(currentLimit)
                .build();
    }
}
