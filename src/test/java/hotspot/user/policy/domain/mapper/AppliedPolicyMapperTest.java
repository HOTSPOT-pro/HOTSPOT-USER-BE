package hotspot.user.policy.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.Member;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.subscription.domain.Subscription;

class AppliedPolicyMapperTest {

    @Test
    @DisplayName("성공: 개인별 정책 정보(시간+앱차단)를 통합하여 AppliedPolicyResponse를 생성한다")
    void toAppliedPolicyResponseSuccess() {
        // given
        Long memberId = 1L;
        Long subId = 100L;
        Long policyId = 50L;
        Long appId = 200L;

        Member member = Member.builder().id(memberId).name("홍길동").build();
        Subscription sub = Subscription.builder().id(subId).member(member).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(sub)
                .dataLimit(1024 * 1024 * 2) // 2GB (KB 단위라고 가정 시 kbToGb 변환 결과 확인용)
                .priority(1)
                .build();

        // 1. 시간 정책 설정
        PolicySub policySub = PolicySub.builder().blockPolicyId(policyId).isActive(true).build();
        BlockPolicy blockPolicy = BlockPolicy.builder().id(policyId).name("밤 10시 차단").build();
        Map<Long, BlockPolicy> policyMap = Map.of(policyId, blockPolicy);

        // 2. 앱 차단 설정
        BlockedServiceSub blockedSub = BlockedServiceSub.builder().appBlockedServiceId(appId).isActive(true).build();
        AppBlockedService app = AppBlockedService.builder().id(appId).name("YouTube").build();
        Map<Long, AppBlockedService> appMap = Map.of(appId, app);

        // when
        AppliedPolicyResponse response = AppliedPolicyMapper.toAppliedPolicyResponse(
                familySub,
                List.of(policySub),
                List.of(blockedSub),
                policyMap,
                appMap
        );

        // then
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.memberName()).isEqualTo("홍길동");
        assertThat(response.subId()).isEqualTo(subId);
        assertThat(response.blockPolicyResponseList()).hasSize(1);
        assertThat(response.blockPolicyResponseList().get(0).name()).isEqualTo("밤 10시 차단");
        assertThat(response.appBlockedServiceResponseList()).hasSize(1);
        assertThat(response.appBlockedServiceResponseList().get(0).name()).isEqualTo("YouTube");
    }

    @Test
    @DisplayName("성공: 상세 정보(Map)가 없는 정책이나 앱은 결과 리스트에서 제외(필터링)된다")
    void toAppliedPolicyResponseFilteringNull() {
        // given
        FamilySubscription familySub = FamilySubscription.builder()
                .subscription(Subscription.builder().id(100L).member(Member.builder().id(1L).build()).build())
                .build();

        // 매핑 정보는 있으나 Map에는 정보가 없는 상황
        PolicySub policySub = PolicySub.builder().blockPolicyId(999L).build();
        BlockedServiceSub blockedSub = BlockedServiceSub.builder().appBlockedServiceId(888L).build();

        Map<Long, BlockPolicy> emptyPolicyMap = Map.of();
        Map<Long, AppBlockedService> emptyAppMap = Map.of();

        // when
        AppliedPolicyResponse response = AppliedPolicyMapper.toAppliedPolicyResponse(
                familySub,
                List.of(policySub),
                List.of(blockedSub),
                emptyPolicyMap,
                emptyAppMap
        );

        // then: 정보가 없는 항목은 필터링되어 리스트가 비어있어야 함
        assertThat(response.blockPolicyResponseList()).isEmpty();
        assertThat(response.appBlockedServiceResponseList()).isEmpty();
    }
}
