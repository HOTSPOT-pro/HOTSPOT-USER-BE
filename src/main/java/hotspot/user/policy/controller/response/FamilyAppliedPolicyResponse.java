package hotspot.user.policy.controller.response;

import java.util.List;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.PriorityType;
import lombok.Builder;

/**
 * 가족 전체 구성원의 적용된 정책 조회 응답 dto
 */
@Builder
public record FamilyAppliedPolicyResponse(
        Long familyId,
        int familyNum,
        int familyDataAmount,
        PriorityType priorityType,
        List<AppliedPolicyResponse> memberPolicies
) {
    /**
     * Family 도메인과 조립된 memberPolicies 리스트를 사용하여 DTO를 생성한다.
     */
    public static FamilyAppliedPolicyResponse from(Family family, List<AppliedPolicyResponse> memberPolicies) {
        return FamilyAppliedPolicyResponse.builder()
                .familyId(family.getId())
                .familyNum(family.getFamilyNum())
                .familyDataAmount(family.getFamilyDataAmount())
                .priorityType(family.getPriorityType())
                .memberPolicies(memberPolicies)
                .build();
    }
}
