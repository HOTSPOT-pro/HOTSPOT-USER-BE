package hotspot.user.policy.controller.response;

import java.util.List;

import hotspot.user.family.domain.PriorityType;
import lombok.Builder;

/**
 * 가족 전체 구성원의 적용된 정책 조회 응답 dto
 */
@Builder
public record FamilyAppliedPolicyResponse(
        Long familyId,
        int familyNum,
        double familyDataAmount,
        PriorityType priorityType,
        List<AppliedPolicyResponse> memberPolicies
) {
}
