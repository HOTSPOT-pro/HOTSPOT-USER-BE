package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
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
import hotspot.user.member.domain.Member;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
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

/**
 * 구성원 1명 적용 정책 조회 Service 단위 테스트
 */
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

    @InjectMocks
    private FindMemberAppliedPolicyServiceImpl findMemberAppliedPolicyService;

    @Test
    @DisplayName("멤버 ID로 적용된 모든 정책 정보(시간+앱차단)를 통합 조회한다")
    void findAppliedPoliciesSuccess() {
        // given
        Long memberId = 1L;
        Long subId = 100L;
        Long policyId = 50L;
        Long appId = 200L;

        Member member = Member.builder().id(memberId).name("홍길동").build();
        Subscription sub = Subscription.builder().id(subId).member(member).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(sub)
                .dataLimit(1024 * 1024) // 1GB
                .priority(1)
                .build();

        // 활성 정책 매핑 (SCHEDULED는 만료 체크 대상 아님)
        PolicySub policySub = PolicySub.builder()
                .id(10L)
                .blockPolicyId(policyId)
                .isActive(true)
                .modifiedTime(LocalDateTime.now())
                .build();

        // 정책 상세 정보
        BlockPolicy blockPolicy = BlockPolicy.builder()
                .id(policyId)
                .name("수면 모드")
                .policyType(PolicyType.SCHEDULED)
                .build();

        // 앱 차단 매핑 (ID 기반)
        BlockedServiceSub blockedSub = BlockedServiceSub.builder()
                .id(20L)
                .subId(subId)
                .appBlockedServiceId(appId)
                .isActive(true)
                .build();
        // 앱 차단 상세 정보
        AppBlockedService app = AppBlockedService.builder().id(appId).name("YouTube").serviceCode("YOUTUBE").build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));
        given(policySubRepository.findActiveBySubId(subId)).willReturn(List.of(policySub));
        given(blockedServiceSubRepository.findActiveBySubId(subId)).willReturn(List.of(blockedSub));
        given(blockPolicyRepository.findAllById(List.of(policyId))).willReturn(List.of(blockPolicy));
        given(appBlockedServiceRepository.findAllByAppBlockedServiceIds(anyList())).willReturn(List.of(app));

        // when
        AppliedPolicyResponse response = findMemberAppliedPolicyService.findByMemberId(memberId);

        // then
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.memberName()).isEqualTo("홍길동");
        assertThat(response.blockPolicyResponseList()).hasSize(1);
        assertThat(response.appBlockedServiceResponseList()).hasSize(1);
        assertThat(response.blockPolicyResponseList().get(0).name()).isEqualTo("수면 모드");
    }

    @Test
    @DisplayName("성공: 만료된 일회성(ONCE) 정책은 조회 시 비활성화 처리되고 응답에서 제외된다")
    void findAppliedPoliciesWithLazyDeactivation() {
        // given
        Long memberId = 1L;
        Long subId = 100L;
        Long activePolicyId = 50L;
        Long expiredPolicyId = 51L;

        Member member = Member.builder().id(memberId).name("홍길동").build();
        Subscription sub = Subscription.builder().id(subId).member(member).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(sub)
                .build();

        // 1. 정상 활성 정책 (SCHEDULED)
        PolicySub activeSub = PolicySub.builder()
                .id(10L).blockPolicyId(activePolicyId).isActive(true).modifiedTime(LocalDateTime.now()).build();

        // 2. 만료된 일회성 정책 (ONCE, 30분 지속인데 40분 전 활성화됨)
        PolicySub expiredSub = PolicySub.builder()
                .id(11L)
                .blockPolicyId(expiredPolicyId)
                .isActive(true)
                .modifiedTime(LocalDateTime.now().minusMinutes(40))
                .build();

        BlockPolicy activePolicy = BlockPolicy.builder()
                .id(activePolicyId).name("상시 정책").policyType(PolicyType.SCHEDULED).build();

        BlockPolicy expiredPolicy = BlockPolicy.builder()
                .id(expiredPolicyId)
                .name("일회성 정책")
                .policyType(PolicyType.ONCE)
                .policySnapshot(PolicySnapshot.builder().durationMinutes(30).build())
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));
        given(policySubRepository.findActiveBySubId(subId)).willReturn(List.of(activeSub, expiredSub));
        given(blockPolicyRepository.findAllById(anyList())).willReturn(List.of(activePolicy, expiredPolicy));
        given(blockedServiceSubRepository.findActiveBySubId(subId)).willReturn(List.of());

        // when
        AppliedPolicyResponse response = findMemberAppliedPolicyService.findByMemberId(memberId);

        // then
        // 1. 응답에는 활성 정책 1개만 포함되어야 함
        assertThat(response.blockPolicyResponseList()).hasSize(1);
        assertThat(response.blockPolicyResponseList().get(0).id()).isEqualTo(activePolicyId);

        // 2. 만료된 정책에 대해 비활성화 저장 로직이 호출되었는지 검증
        ArgumentCaptor<List<PolicySub>> captor = ArgumentCaptor.forClass(List.class);
        verify(policySubRepository, times(1)).saveAll(captor.capture());

        List<PolicySub> savedPolicies = captor.getValue();
        assertThat(savedPolicies).hasSize(1);
        assertThat(savedPolicies.get(0).getId()).isEqualTo(11L);
        assertThat(savedPolicies.get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("멤버 정보가 존재하지 않으면 예외가 발생한다")
    void findAppliedPoliciesFail() {
        // given
        Long invalidId = 999L;
        given(familySubscriptionRepository.findByMemberId(invalidId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findMemberAppliedPolicyService.findByMemberId(invalidId))
                .isExactlyInstanceOf(ApplicationException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
