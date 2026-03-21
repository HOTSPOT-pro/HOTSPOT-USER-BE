package hotspot.user.policy.controller.response;

import java.util.List;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 개인별 적용된 정책 조회 응답 dto
 */

@Builder
public record AppliedPolicyResponse(
        Long memberId,
        String memberName,
        Long subId,
        FamilyRole role,
        double familyDataSubLimit,
        double familyDataUsage,
        int priority,
        boolean isBlocked,
        List<BlockPolicyResponse> blockPolicyResponseList,
        List<AppBlockedServiceResponse> appBlockedServiceResponseList
) {
}
