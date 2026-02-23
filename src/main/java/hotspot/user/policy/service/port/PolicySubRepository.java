package hotspot.user.policy.service.port;

import java.util.List;

import hotspot.user.policy.domain.PolicySub;

/**
 * 정책-회선 매핑 테이블 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface PolicySubRepository {
    List<PolicySub> findBySubId(Long subId);
    List<PolicySub> saveAll(List<PolicySub> policySubList);
}
