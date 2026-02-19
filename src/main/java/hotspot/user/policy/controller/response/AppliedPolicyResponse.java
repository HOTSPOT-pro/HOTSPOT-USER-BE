package hotspot.user.policy.controller.response;

import java.util.List;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.AppliedPolicyMapper;
import lombok.Builder;

/**
 * 개인별 적용된 정책 조회 응답 dto
 */
@Builder
public record AppliedPolicyResponse(
        Long memberId,
        String memberName,
        int dataLimit, // 개인별 한도
        int priority, // 구성원 내 우선 순위
        List<BlockPolicyResponse> blockPolicyResponseList,
        List<AppBlockedServiceResponse> appBlockedServiceResponseList
) {
    /**
     * 도메인 객체들을 조합하여 AppliedPolicyResponse DTO를 생성한다.
     */
    public static AppliedPolicyResponse from(
            FamilySubscription familySub,
            List<PolicySub> policySubList,
            List<BlockedServiceSub> blockedServiceSubList
    ) {
        return AppliedPolicyResponse.builder()
                .memberId(familySub.getSubscription().getMember().getId())
                .memberName(familySub.getSubscription().getMember().getName())
                .dataLimit(familySub.getDataLimit())
                .priority(familySub.getPriority())
                .blockPolicyResponseList(policySubList.stream()
                        .map(AppliedPolicyMapper::toBlockPolicyResponse)
                        .toList())
                .appBlockedServiceResponseList(blockedServiceSubList.stream()
                        .map(AppliedPolicyMapper::toAppBlockedServiceResponse)
                        .toList())
                .build();
    }
}
