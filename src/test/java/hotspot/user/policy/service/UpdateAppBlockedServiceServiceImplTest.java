package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.app.AppBlockListUpdateEvent;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;

@ExtendWith(MockitoExtension.class)
class UpdateAppBlockedServiceServiceImplTest {

    @Mock
    private BlockedServiceSubRepository blockedServiceSubRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    @Mock
    private UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateAppBlockedServiceServiceImpl service;

    @Test
    @DisplayName("성공: OWNER가 동일 가족 구성원의 앱 차단 설정을 업데이트한다")
    void updateAppBlockedServiceSuccess() {
        // given
        Long familyId = 1L;
        Long subId = 100L;
        Long requesterFamilyId = 1L;
        List<Long> targetIds = List.of(2L, 3L);
        UpdateAppBlockedServiceRequest request = new UpdateAppBlockedServiceRequest(familyId, subId, targetIds);

        FamilySubscription familySub = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .build();

        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));

        // 기존 상태: 1(활성), 2(활성)
        List<BlockedServiceSub> existingSubs = List.of(
                BlockedServiceSub.builder().id(10L).subId(subId).appBlockedServiceId(1L).isActive(true).build(),
                BlockedServiceSub.builder().id(11L).subId(subId).appBlockedServiceId(2L).isActive(true).build()
        );
        given(blockedServiceSubRepository.findBySubId(subId)).willReturn(existingSubs);

        // 모든 필요한 앱 서비스 정보 조회 (1, 2, 3)
        given(appBlockedServiceRepository.findAllActiveAndInDeleteByAppBlockedServiceIds(anyList()))
                .willReturn(List.of(
                        AppBlockedService.builder().id(1L).name("YouTube").isActive(true).build(),
                        AppBlockedService.builder().id(2L).name("TikTok").isActive(true).build(),
                        AppBlockedService.builder().id(3L).name("Instagram").isActive(true).build()
                ));

        // when
        UpdateAppBlockedServiceResponse response =
                service.updateAppBlockedService(request, requesterFamilyId, FamilyRole.OWNER);

        // then
        assertThat(response.subId()).isEqualTo(subId);
        assertThat(response.blockedServiceIdList()).containsExactlyInAnyOrder(2L, 3L);

        // 변경분 검증:
        // 1번: 활성 -> 비활성
        // 2번: 활성 유지 (변경 없음)
        // 3번: 신규 추가 (활성)
        verify(blockedServiceSubRepository, times(1)).saveAll(anyList());
        verify(eventPublisher, times(1)).publishEvent(any(AppBlockListUpdateEvent.class));
    }

    @Test
    @DisplayName("실패: 요청된 앱 ID 중 일부가 존재하지 않으면 예외가 발생한다")
    void updateAppBlockedServiceFailInvalidAppId() {
        // given
        Long familyId = 1L;
        Long subId = 100L;
        UpdateAppBlockedServiceRequest request = new UpdateAppBlockedServiceRequest(familyId, subId, List.of(999L));

        FamilySubscription familySub = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .build();

        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));
        given(blockedServiceSubRepository.findBySubId(subId)).willReturn(List.of());

        // 999L은 DB에 없음
        given(appBlockedServiceRepository
                .findAllActiveAndInDeleteByAppBlockedServiceIds(anyList()))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> service.updateAppBlockedService(request, 1L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.BLOCKED_SERVICE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아니면 예외가 발생한다")
    void updateAppBlockedServiceFailNotOwner() {
        // given
        UpdateAppBlockedServiceRequest request = new UpdateAppBlockedServiceRequest(1L, 100L, List.of(1L));

        // when & then
        assertThatThrownBy(() -> service.updateAppBlockedService(request, 1L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: 다른 가족 구성원의 설정을 변경하려 하면 예외가 발생한다")
    void updateAppBlockedServiceFailDifferentFamily() {
        // given
        Long requesterFamilyId = 1L;
        Long targetFamilyId = 2L;
        Long subId = 100L;
        UpdateAppBlockedServiceRequest request = new UpdateAppBlockedServiceRequest(targetFamilyId, subId, List.of(1L));

        FamilySubscription familySub = FamilySubscription.builder()
                .family(Family.builder().id(targetFamilyId).build())
                .build();

        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> service.updateAppBlockedService(request, requesterFamilyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }
}
