package hotspot.user.policy.controller.port;

import hotspot.user.policy.controller.response.AppliedPolicyResponse;

/**
 * 특정 구성원의 적용된 통합 정책 조회 서비스
 */
public interface FindMemberAppliedPolicyService {
    AppliedPolicyResponse findByMemberId(Long memberId);
}
