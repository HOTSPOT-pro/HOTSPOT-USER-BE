package hotspot.user.policy.service.port;

import java.util.List;

import hotspot.user.policy.domain.PolicySub;

/**
 * 정책-회선 매핑 테이블 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface PolicySubRepository {
    List<PolicySub> findBySubId(Long subId);

    List<PolicySub> findActiveBySubId(Long subId); // 활성화된 정책만 조회

    List<PolicySub> saveAll(List<PolicySub> policySubList);

    // 비활성화된 정책이 적용되어 있는 policy_sub 모두 isActive = false로 만들기
    void bulkDeActiveByBlockPolicyIds(List<Long> blockPolicyIds);
}
