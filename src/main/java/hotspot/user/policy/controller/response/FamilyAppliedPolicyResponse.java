package hotspot.user.policy.controller.response;

import java.util.List;

import hotspot.user.family.domain.PriorityType;
import lombok.Builder;

/**
 * 가족 전체 구성원의 적용된 정책 조회 응답 dto
 * @param familyId
 * @param familyNum
 * @param familyDataAmount
 * @param priorityType
 * @param memberPolicies
 */
@Builder
public record FamilyAppliedPolicyResponse(
        Long familyId,
        int familyNum, // 가족 구성원 수
        int familyDataAmount, // 가족 데이터 한도량
        PriorityType priorityType, // 가족의 데이터 사용 우선순위 방식 (FIFO or PRIORITY)
        List<AppliedPolicyResponse> memberPolicies
) {
}
