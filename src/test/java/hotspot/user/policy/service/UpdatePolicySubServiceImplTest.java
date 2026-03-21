package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.policy.service.util.PolicyBlockSnapshotPublisher;

@ExtendWith(MockitoExtension.class)
class UpdatePolicySubServiceImplTest {

    @InjectMocks
    private UpdatePolicySubServiceImpl updatePolicySubService;

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;

    @Mock
    private PolicyBlockSnapshotPublisher policyBlockSnapshotPublisher;

    @Test
    @DisplayName("성공: 신규 정책 추가 - DB에 매핑이 없을 경우 새로 생성된다")
    void updatePolicySubSuccessWithNew() {

        Long requesterMemberId = 1000L;
        Long familyId = 100L;
        Long subId = 1L;

        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(familyId, subId, List.of(1L));

        setAuthMock(requesterMemberId, familyId, subId);

        given(policySubRepository.findBySubId(subId))
                .willReturn(new ArrayList<>());

        BlockPolicy policy =
                BlockPolicy.builder()
                        .id(1L)
                        .name("Test Policy")
                        .isActive(true)
                        .build();

        given(blockPolicyRepository.findAllById(anyList()))
                .willReturn(List.of(policy));

        given(policySubRepository.findActiveBySubId(subId))
                .willReturn(new ArrayList<>());

        UpdatePolicySubResponse response =
                updatePolicySubService.updatePolicySub(
                        request,
                        requesterMemberId,
                        FamilyRole.OWNER
                );

        // then
        assertThat(response.subId()).isEqualTo(subId);

        verify(policySubRepository, times(1)).saveAll(anyList());
        verify(policyBlockSnapshotPublisher, times(1))
                .publish(anyLong(), anyList());
    }

    @Test
    @DisplayName("성공: 기존 비활성 정책 재활용 - 이미 매핑이 있지만 비활성인 경우 활성화된다")
    void updatePolicySubSuccessWithActivation() {

        Long requesterMemberId = 1000L;
        Long familyId = 100L;
        Long subId = 1L;

        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(familyId, subId, List.of(1L));

        setAuthMock(requesterMemberId, familyId, subId);

        PolicySub existingSub =
                PolicySub.builder()
                        .id(10L)
                        .blockPolicyId(1L)
                        .isActive(false)
                        .build();

        given(policySubRepository.findBySubId(subId))
                .willReturn(new ArrayList<>(List.of(existingSub)));

        BlockPolicy policy =
                BlockPolicy.builder()
                        .id(1L)
                        .name("Test Policy")
                        .isActive(true)
                        .build();

        given(blockPolicyRepository.findAllById(anyList()))
                .willReturn(List.of(policy));

        given(policySubRepository.findActiveBySubId(subId))
                .willReturn(new ArrayList<>());

        updatePolicySubService.updatePolicySub(
                request,
                requesterMemberId,
                FamilyRole.OWNER
        );

        assertThat(existingSub.isActive()).isTrue();

        verify(policySubRepository, times(1)).saveAll(anyList());
        verify(policyBlockSnapshotPublisher, times(1))
                .publish(anyLong(), anyList());
    }

    @Test
    @DisplayName("성공: 요청 목록에 없는 기존 정책은 비활성화된다")
    void updatePolicySubSuccessWithDeactivation() {

        Long requesterMemberId = 1000L;
        Long familyId = 100L;
        Long subId = 1L;

        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(familyId, subId, List.of());

        setAuthMock(requesterMemberId, familyId, subId);

        PolicySub existingSub =
                PolicySub.builder()
                        .id(10L)
                        .blockPolicyId(1L)
                        .isActive(true)
                        .build();

        given(policySubRepository.findBySubId(subId))
                .willReturn(new ArrayList<>(List.of(existingSub)));

        BlockPolicy existingPolicy =
                BlockPolicy.builder()
                        .id(1L)
                        .name("Existing Policy")
                        .isActive(true)
                        .build();

        given(blockPolicyRepository.findAllById(anyList()))
                .willReturn(List.of(existingPolicy));

        given(policySubRepository.findActiveBySubId(subId))
                .willReturn(new ArrayList<>());

        updatePolicySubService.updatePolicySub(
                request,
                requesterMemberId,
                FamilyRole.OWNER
        );

        assertThat(existingSub.isActive()).isFalse();

        verify(policySubRepository, times(1)).saveAll(anyList());
        verify(policyBlockSnapshotPublisher, times(1))
                .publish(anyLong(), anyList());
    }

    @Test
    @DisplayName("실패: 타 가족의 정책을 적용하려 하면 예외가 발생한다")
    void updatePolicySubFailByPolicyAccessDenied() {

        Long requesterMemberId = 1000L;
        Long myFamilyId = 100L;
        Long otherFamilyId = 200L;

        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(myFamilyId, 1L, List.of(1L));

        setAuthMock(requesterMemberId, myFamilyId, 1L);

        given(policySubRepository.findBySubId(1L))
                .willReturn(new ArrayList<>());

        BlockPolicy otherPolicy =
                BlockPolicy.builder()
                        .id(1L)
                        .familyId(otherFamilyId)
                        .build();

        given(blockPolicyRepository.findAllById(anyList()))
                .willReturn(List.of(otherPolicy));

        assertThatThrownBy(() ->
                updatePolicySubService.updatePolicySub(
                        request,
                        requesterMemberId,
                        FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.POLICY_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: 현재 비활성 상태인 정책을 적용하려 하면 예외가 발생한다")
    void updatePolicySubFailByInactivePolicy() {

        Long requesterMemberId = 1000L;
        Long familyId = 100L;

        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(familyId, 1L, List.of(1L));

        setAuthMock(requesterMemberId, familyId, 1L);

        given(policySubRepository.findBySubId(1L))
                .willReturn(new ArrayList<>());

        BlockPolicy inactivePolicy =
                BlockPolicy.builder()
                        .id(1L)
                        .isActive(false)
                        .build();

        given(blockPolicyRepository.findAllById(anyList()))
                .willReturn(List.of(inactivePolicy));

        assertThatThrownBy(() ->
                updatePolicySubService.updatePolicySub(
                        request,
                        requesterMemberId,
                        FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.INACTIVE_POLICY_CANNOT_APPLY.getMessage());
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아닌 경우 예외가 발생한다")
    void updatePolicySubFailByRole() {

        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(100L, 1L, List.of(1L));

        assertThatThrownBy(() ->
                updatePolicySubService.updatePolicySub(
                        request,
                        1000L,
                        FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: 요청한 정책 중 일부가 존재하지 않으면 예외가 발생한다")
    void updatePolicySubFailByPolicyNotFound() {

        Long requesterMemberId = 1000L;
        UpdatePolicySubRequest request =
                new UpdatePolicySubRequest(100L, 1L, List.of(1L, 2L));

        setAuthMock(requesterMemberId, 100L, 1L);

        given(policySubRepository.findBySubId(1L))
                .willReturn(new ArrayList<>());

        given(blockPolicyRepository.findAllById(anyList()))
                .willReturn(List.of(
                        BlockPolicy.builder()
                                .id(1L)
                                .isActive(true)
                                .build()
                ));

        assertThatThrownBy(() ->
                updatePolicySubService.updatePolicySub(
                        request,
                        requesterMemberId,
                        FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.POLICY_NOT_FOUND.getMessage());
    }

    private void setAuthMock(Long requesterMemberId, Long familyId, Long subId) {

        Family family =
                Family.builder()
                        .id(familyId)
                        .build();

        FamilySubscription familySub =
                FamilySubscription.builder()
                        .family(family)
                        .build();

        // 요청자의 소속 조회
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(familySub));

        // 타겟 subId의 소속 조회
        given(familySubscriptionRepository.findBySubId(subId))
                .willReturn(Optional.of(familySub));
    }
}
