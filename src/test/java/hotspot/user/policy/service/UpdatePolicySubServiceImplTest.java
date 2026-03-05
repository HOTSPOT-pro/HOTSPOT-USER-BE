package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
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

    @Test
    @DisplayName("성공: 신규 정책 추가 - DB에 매핑이 없을 경우 새로 생성된다")
    void updatePolicySubSuccessWithNew() {
        // given
        Long familyId = 100L;
        Long subId = 1L;
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(familyId, subId, List.of(1L));

        setAuthMock(familyId, subId);

        // 1. 기존 매핑 조회 (비어있음)
        given(policySubRepository.findBySubId(subId)).willReturn(new ArrayList<>());

        // 2. 통합 정책 상세 조회 (요청한 1L 조회)
        BlockPolicy policy = BlockPolicy.builder().id(1L).name("Test Policy").isActive(true).build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(policy));

        // when
        UpdatePolicySubResponse response = updatePolicySubService.updatePolicySub(
                request,
                familyId,
                FamilyRole.OWNER);

        // then
        assertThat(response.subId()).isEqualTo(subId);
        verify(policySubRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("성공: 기존 비활성 정책 재활용 - 이미 매핑이 있지만 비활성인 경우 활성화된다")
    void updatePolicySubSuccessWithActivation() {
        // given
        Long familyId = 100L;
        Long subId = 1L;
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(familyId, subId, List.of(1L));

        setAuthMock(familyId, subId);

        // 1. 기존 비활성 매핑 (ID: 1L)
        PolicySub existingSub = PolicySub.builder()
                .id(10L)
                .blockPolicyId(1L)
                .isActive(false)
                .modifiedTime(LocalDateTime.now().minusDays(1))
                .build();
        given(policySubRepository.findBySubId(subId)).willReturn(new ArrayList<>(List.of(existingSub)));

        // 2. 통합 정책 상세 조회 (기존/요청 1L 조회)
        BlockPolicy policy = BlockPolicy.builder().id(1L).name("Test Policy").isActive(true).build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(policy));

        // when
        updatePolicySubService.updatePolicySub(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(existingSub.isActive()).isTrue(); // 다시 활성화됨
        verify(policySubRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("성공: 요청 목록에 없는 기존 정책은 비활성화된다")
    void updatePolicySubSuccessWithDeactivation() {
        // given
        Long familyId = 100L;
        Long subId = 1L;
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(familyId, subId, List.of()); // 요청은 비어있음

        setAuthMock(familyId, subId);

        // 1. 기존 활성 매핑 (ID: 1L)
        PolicySub existingSub = PolicySub.builder()
                .id(10L)
                .blockPolicyId(1L)
                .isActive(true)
                .modifiedTime(LocalDateTime.now())
                .build();
        given(policySubRepository.findBySubId(subId)).willReturn(new ArrayList<>(List.of(existingSub)));

        // 2. 통합 정책 상세 조회 (기존 1L 조회)
        BlockPolicy existingPolicy = BlockPolicy.builder().id(1L).name("Existing Policy").isActive(true).build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(existingPolicy));

        // when
        updatePolicySubService.updatePolicySub(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(existingSub.isActive()).isFalse(); // 비활성화됨
        verify(policySubRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("실패: 타 가족의 정책을 적용하려 하면 예외가 발생한다")
    void updatePolicySubFailByPolicyAccessDenied() {
        // given
        Long myFamilyId = 100L;
        Long otherFamilyId = 200L;
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(myFamilyId, 1L, List.of(1L));

        setAuthMock(myFamilyId, 1L);

        // 1. 기존 매핑 조회 (비어있음)
        given(policySubRepository.findBySubId(1L)).willReturn(new ArrayList<>());

        // 2. 통합 정책 상세 조회 (타 가족의 정책)
        BlockPolicy otherPolicy = BlockPolicy.builder().id(1L).familyId(otherFamilyId).build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(otherPolicy));

        // when & then
        assertThatThrownBy(() -> updatePolicySubService.updatePolicySub(request, myFamilyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.POLICY_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: 현재 비활성 상태인 정책을 적용하려 하면 예외가 발생한다")
    void updatePolicySubFailByInactivePolicy() {
        // given
        Long familyId = 100L;
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(familyId, 1L, List.of(1L));

        setAuthMock(familyId, 1L);

        // 1. 기존 매핑 조회 (비어있음)
        given(policySubRepository.findBySubId(1L)).willReturn(new ArrayList<>());

        // 2. 통합 정책 상세 조회 (비활성 정책)
        BlockPolicy inactivePolicy = BlockPolicy.builder().id(1L).isActive(false).build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(inactivePolicy));

        // when & then
        assertThatThrownBy(() -> updatePolicySubService.updatePolicySub(request, familyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.INACTIVE_POLICY_CANNOT_APPLY.getMessage());
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아닌 경우 예외가 발생한다")
    void updatePolicySubFailByRole() {
        // given
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(100L, 1L, List.of(1L));

        // when & then
        assertThatThrownBy(() -> updatePolicySubService.updatePolicySub(request, 100L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: 요청한 정책 중 일부가 존재하지 않으면 예외가 발생한다")
    void updatePolicySubFailByPolicyNotFound() {
        // given
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(100L, 1L, List.of(1L, 2L));
        setAuthMock(100L, 1L);

        // 1. 기존 매핑 조회 (비어있음)
        given(policySubRepository.findBySubId(1L)).willReturn(new ArrayList<>());

        // 2. 통합 정책 상세 조회 (1L만 존재하고 2L은 누락됨)
        given(blockPolicyRepository.findAllById(anyList())).willReturn(
                List.of(BlockPolicy.builder()
                        .id(1L)
                        .isActive(true)
                        .build()));

        // when & then
        assertThatThrownBy(() -> updatePolicySubService.updatePolicySub(request, 100L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.POLICY_NOT_FOUND.getMessage());
    }

    private void setAuthMock(Long familyId, Long subId) {
        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySub = FamilySubscription.builder().family(family).build();
        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));
    }
}
