package hotspot.user.policy.controller.response;

import java.util.List;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 개인별 적용된 정책 조회 응답 dto
 * @param memberId
 * @param memberName
 * @param dataLimit
 * @param priority
 * @param familyRole
 * @param blockPolicyResponseList
 * @param appBlockedServiceResponseList
 */
@Builder
public record AppliedPolicyResponse(
        Long memberId,
        String memberName,
        int dataLimit, // 개인별 한도
        int priority, // 구성원 내 우선 순위
        FamilyRole familyRole, // 가족 내 역할
        List<BlockPolicyResponse> blockPolicyResponseList,
        List<AppBlockedServiceResponse> appBlockedServiceResponseList
) {
}
