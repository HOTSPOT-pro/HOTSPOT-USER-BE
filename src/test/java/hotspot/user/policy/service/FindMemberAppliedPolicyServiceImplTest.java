package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.DateSnapshot;
import hotspot.user.policy.domain.PolicySub;
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

    @InjectMocks
    private FindMemberAppliedPolicyServiceImpl findMemberAppliedPolicyService;

    @Test
    @DisplayName("멤버 ID로 적용된 모든 정책 정보(시간+앱차단)를 통합 조회한다")
    void findAppliedPoliciesSuccess() {
        // given
        Long memberId = 1L;
        Long subId = 100L;

        Member member = Member.builder().id(memberId).name("홍길동").build();
        Subscription sub = Subscription.builder().id(subId).member(member).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(sub)
                .dataLimit(10)
                .priority(1)
                .build();

        DateSnapshot snapshot = DateSnapshot.builder().policyName("수면 모드").build();
        PolicySub policySub = PolicySub.builder().id(10L).dateSnapshot(snapshot).build();

        AppBlockedService app = AppBlockedService.builder().name("YouTube").serviceCode("YOUTUBE").build();
        BlockedServiceSub blockedSub = BlockedServiceSub.builder().id(20L).appBlockedService(app).build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));
        given(policySubRepository.findBySubId(subId)).willReturn(List.of(policySub));
        given(blockedServiceSubRepository.findBySubId(subId)).willReturn(List.of(blockedSub));

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
