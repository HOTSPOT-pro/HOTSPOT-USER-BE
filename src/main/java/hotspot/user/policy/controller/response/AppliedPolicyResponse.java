package hotspot.user.policy.controller.response;

import java.util.List;

import lombok.Builder;

/**
 * 개인별 적용된 정책 조회 응답 dto
 */
@Builder
public record AppliedPolicyResponse(
        Long memberId,
        String memberName,
        Long subId,
        int dataLimit, // 개인별 한도
        int priority, // 구성원 내 우선 순위
        List<BlockPolicyResponse> blockPolicyResponseList,
        List<AppBlockedServiceResponse> appBlockedServiceResponseList
) {
}
