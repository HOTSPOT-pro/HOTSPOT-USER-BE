package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
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
import hotspot.user.kafka.outbox.NotificationUserAlertOutboxPublisher;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.request.UpdateBlockPolicyRequest;
import hotspot.user.policy.controller.response.UpdateBlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;

@ExtendWith(MockitoExtension.class)
class UpdateBlockPolicyServiceImplTest {

    @InjectMocks
    private UpdateBlockPolicyServiceImpl updateBlockPolicyService;

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private NotificationUserAlertOutboxPublisher userAlertOutboxPublisher;

    @Test
    @DisplayName("성공: 유효한 요청일 경우 구성원에게 적용된 정책이 업데이트된다")
    void updateBlockPolicySuccess() {
        // given
        Long familyId = 100L;
        Long subId = 1L;
        UpdateBlockPolicyRequest request = new UpdateBlockPolicyRequest(familyId, subId, List.of(1L));

        setAuthMock(familyId, subId);

        BlockPolicy policy = BlockPolicy.builder().id(1L).name("Test Policy").build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(policy));
        given(policySubRepository.findBySubId(subId)).willReturn(new ArrayList<>());

        // when
        UpdateBlockPolicyResponse response = updateBlockPolicyService.updateBlockPolicy(
                request,
                familyId,
                FamilyRole.OWNER);

        // then
        assertThat(response.subId()).isEqualTo(subId);
        verify(policySubRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("성공: 기존 정책이 교체되고 새로운 정책이 추가된다")
    void updateBlockPolicySuccessWithReplacement() {
        // given
        Long familyId = 100L;
        Long subId = 1L;
        UpdateBlockPolicyRequest request = new UpdateBlockPolicyRequest(familyId, subId, List.of(2L));

        setAuthMock(familyId, subId);

        // 기존에 활성화된 정책 (ID: 1L)
        PolicySub existingSub = PolicySub.builder().id(10L).policyId(1L).isDeleted(false).build();
        given(policySubRepository.findBySubId(subId)).willReturn(new ArrayList<>(List.of(existingSub)));

        // 요청된 새로운 정책 (ID: 2L)
        BlockPolicy newPolicy = BlockPolicy.builder().id(2L).name("New Policy").build();
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(newPolicy));

        // when
        UpdateBlockPolicyResponse response = updateBlockPolicyService.updateBlockPolicy(
                request,
                familyId,
                FamilyRole.OWNER);

        // then
        assertThat(response.blockedPolicyIdList()).containsExactly(2L);
        assertThat(existingSub.isDeleted()).isTrue(); // 기존 정책은 삭제됨
        verify(policySubRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("성공: 요청 목록에 없는 기존 정책은 삭제된다")
    void updateBlockPolicySuccessWithDeletion() {
        // given
        Long familyId = 100L;
        Long subId = 1L;
        UpdateBlockPolicyRequest request = new UpdateBlockPolicyRequest(familyId, subId, List.of()); // 요청은 비어있음

        setAuthMock(familyId, subId);

        PolicySub existingSub = PolicySub.builder().id(10L).policyId(1L).isDeleted(false).build();
        given(policySubRepository.findBySubId(subId)).willReturn(new ArrayList<>(List.of(existingSub)));
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of());

        // when
        updateBlockPolicyService.updateBlockPolicy(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(existingSub.isDeleted()).isTrue();
        verify(policySubRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아닌 경우 예외가 발생한다")
    void updateBlockPolicyFailByRole() {
        // given
        UpdateBlockPolicyRequest request = new UpdateBlockPolicyRequest(100L, 1L, List.of(1L));

        // when & then
        assertThatThrownBy(() -> updateBlockPolicyService.updateBlockPolicy(request, 100L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("?ㅽ뙣: ?붿껌???뺤콉 以??쇰?媛 議댁옱?섏? ?딆쑝硫??덉쇅媛 諛쒖깮?쒕떎")
    void updateBlockPolicyFailByPolicyNotFound() {
        // given
        UpdateBlockPolicyRequest request = new UpdateBlockPolicyRequest(100L, 1L, List.of(1L, 2L));
        setAuthMock(100L, 1L);
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(BlockPolicy.builder().id(1L).build()));

        // when & then
        assertThatThrownBy(() -> updateBlockPolicyService.updateBlockPolicy(request, 100L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PolicyErrorCode.POLICY_NOT_FOUND.getMessage());
    }

    private void setAuthMock(Long familyId, Long subId) {
        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySub = FamilySubscription.builder().family(family).build();
        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySub));
    }
}
