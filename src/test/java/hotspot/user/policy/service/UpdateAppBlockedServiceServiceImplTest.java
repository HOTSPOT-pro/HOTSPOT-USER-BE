package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
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
import hotspot.user.kafka.outbox.NotificationUserAlertOutboxPublisher;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.app.AppBlockListUpdateEvent;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import hotspot.user.policy.domain.AppBlockedService;
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
    private NotificationUserAlertOutboxPublisher userAlertOutboxPublisher;

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
        UpdateAppBlockedServiceRequest request = new UpdateAppBlockedServiceRequest(familyId, subId, List.of(2L, 3L));

        FamilySubscription familySub = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .build();

        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));
        given(appBlockedServiceRepository.countByIdIn(anySet())).willReturn(2L); // 2개 요청 -> 2개 유효
        given(blockedServiceSubRepository.findActiveServiceIdsBySubId(subId)).willReturn(List.of(1L, 2L));
        given(appBlockedServiceRepository.findAllByAppBlockedServiceIds(anyList()))
                .willReturn(List.of(
                        AppBlockedService.builder().id(1L).name("YouTube").build(),
                        AppBlockedService.builder().id(2L).name("TikTok").build(),
                        AppBlockedService.builder().id(3L).name("Instagram").build()
                ));

        // when
        UpdateAppBlockedServiceResponse response =
                service.updateAppBlockedService(request, requesterFamilyId, FamilyRole.OWNER);

        // then
        assertThat(response.subId()).isEqualTo(subId);
        verify(blockedServiceSubRepository, times(1)).saveAll(anyLong(), anySet());
        verify(blockedServiceSubRepository, times(1)).deleteAll(anyLong(), anySet());

        verify(eventPublisher, times(1))
                .publishEvent(any(AppBlockListUpdateEvent.class));
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
        given(appBlockedServiceRepository.countByIdIn(anySet())).willReturn(0L); // 1개 요청 -> 0개 유효

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
