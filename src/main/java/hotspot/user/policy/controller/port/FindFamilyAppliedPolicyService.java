package hotspot.user.policy.controller.port;

import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;

/**
 * 가족 전체 구성원의 적용된 통합 정책 조회 서비스
 */
public interface FindFamilyAppliedPolicyService {
    FamilyAppliedPolicyResponse findByFamilyId(Long familyId);
}
