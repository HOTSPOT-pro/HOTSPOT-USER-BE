package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.policy.controller.port.FindBlockStatusService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockedStatusResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class FindMemberAppliedPolicyServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private BlockedServiceSubRepository blockedServiceSubRepository;

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    @Mock
    private FindBlockStatusService findBlockStatusService;

    @InjectMocks
    private FindMemberAppliedPolicyServiceImpl findMemberAppliedPolicyService;

    @Test
    @DisplayName("멤버 ID로 적용된 모든 정책 정보(시간+앱차단)를 통합 조회한다")
    void findAppliedPoliciesSuccess() {

        Long memberId = 1L;
        Long subId = 100L;
        Long policyId = 50L;
        Long appId = 200L;

        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .build();

        Subscription sub = Subscription.builder()
                .id(subId)
                .member(member)
                .build();

        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(sub)
                .familyRole(FamilyRole.CHILD)
                .dataLimit(1024 * 1024)
                .priority(1)
                .build();

        PolicySub policySub = PolicySub.builder()
                .id(10L)
                .blockPolicyId(policyId)
                .isActive(true)
                .modifiedTime(LocalDateTime.now())
                .build();

        BlockPolicy blockPolicy = BlockPolicy.builder()
                .id(policyId)
                .name("수면 모드")
                .policyType(PolicyType.SCHEDULED)
                .build();

        BlockedServiceSub blockedSub = BlockedServiceSub.builder()
                .id(20L)
                .subId(subId)
                .appBlockedServiceId(appId)
                .isActive(true)
                .build();

        AppBlockedService app = AppBlockedService.builder()
                .id(appId)
                .name("YouTube")
                .serviceCode("YOUTUBE")
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(familySub));

        given(policySubRepository.findActiveBySubId(subId))
                .willReturn(List.of(policySub));

        given(blockPolicyRepository.findAllById(List.of(policyId)))
                .willReturn(List.of(blockPolicy));

        given(blockedServiceSubRepository.findActiveBySubId(subId))
                .willReturn(List.of(blockedSub));

        given(appBlockedServiceRepository.findAllActiveAndInDeleteByAppBlockedServiceIds(List.of(appId)))
                .willReturn(List.of(app));

        given(findBlockStatusService.findMyBlockStatus(memberId))
                .willReturn(BlockedStatusResponse.builder()
                        .isCurrentlyBlocked(true)
                        .build());

        AppliedPolicyResponse response =
                findMemberAppliedPolicyService.findByMemberId(memberId);

        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.memberName()).isEqualTo("홍길동");
        assertThat(response.role()).isEqualTo(FamilyRole.CHILD);
        assertThat(response.isBlocked()).isTrue();

        assertThat(response.blockPolicyResponseList()).hasSize(1);
        assertThat(response.appBlockedServiceResponseList()).hasSize(1);

        assertThat(response.blockPolicyResponseList().get(0).name())
                .isEqualTo("수면 모드");
    }

    @Test
    @DisplayName("만료된 ONCE 정책은 조회 시 자동 비활성화된다")
    void findAppliedPoliciesWithLazyDeactivation() {

        Long memberId = 1L;
        Long subId = 100L;

        Long activePolicyId = 50L;
        Long expiredPolicyId = 51L;

        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .build();

        Subscription sub = Subscription.builder()
                .id(subId)
                .member(member)
                .build();

        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(sub)
                .familyRole(FamilyRole.CHILD)
                .build();

        PolicySub activeSub = PolicySub.builder()
                .id(10L)
                .blockPolicyId(activePolicyId)
                .isActive(true)
                .modifiedTime(LocalDateTime.now())
                .build();

        PolicySub expiredSub = PolicySub.builder()
                .id(11L)
                .blockPolicyId(expiredPolicyId)
                .isActive(true)
                .modifiedTime(LocalDateTime.now().minusMinutes(40))
                .build();

        BlockPolicy activePolicy = BlockPolicy.builder()
                .id(activePolicyId)
                .name("상시 정책")
                .policyType(PolicyType.SCHEDULED)
                .build();

        BlockPolicy expiredPolicy = BlockPolicy.builder()
                .id(expiredPolicyId)
                .name("일회성 정책")
                .policyType(PolicyType.ONCE)
                .policySnapshot(
                        PolicySnapshot.builder()
                                .durationMinutes(30)
                                .build()
                )
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(familySub));

        given(policySubRepository.findActiveBySubId(subId))
                .willReturn(List.of(activeSub, expiredSub));

        given(blockPolicyRepository.findAllById(List.of(activePolicyId, expiredPolicyId)))
                .willReturn(List.of(activePolicy, expiredPolicy));

        given(blockedServiceSubRepository.findActiveBySubId(subId))
                .willReturn(List.of());

        given(findBlockStatusService.findMyBlockStatus(memberId))
                .willReturn(BlockedStatusResponse.builder()
                        .isCurrentlyBlocked(false)
                        .build());

        AppliedPolicyResponse response =
                findMemberAppliedPolicyService.findByMemberId(memberId);

        // 응답에는 활성 정책만 남아야 함
        assertThat(response.blockPolicyResponseList()).hasSize(1);
        assertThat(response.blockPolicyResponseList().get(0).id())
                .isEqualTo(activePolicyId);

        // deactivate 저장 검증
        ArgumentCaptor<List<PolicySub>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(policySubRepository, times(1)).saveAll(captor.capture());

        List<PolicySub> savedPolicies = captor.getValue();

        assertThat(savedPolicies).hasSize(1);
        assertThat(savedPolicies.get(0).getId()).isEqualTo(11L);
        assertThat(savedPolicies.get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("멤버가 존재하지 않으면 예외 발생")
    void findAppliedPoliciesFail() {

        Long invalidId = 999L;

        given(familySubscriptionRepository.findByMemberId(invalidId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                findMemberAppliedPolicyService.findByMemberId(invalidId))
                .isExactlyInstanceOf(ApplicationException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
