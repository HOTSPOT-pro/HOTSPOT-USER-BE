package hotspot.user.policy.service.port;

import hotspot.user.policy.domain.PolicySub;

import java.util.List;

/**
 * 정책-회선 매핑 테이블 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface PolicySubRepository {
    List<PolicySub> findBySubId(Long subId);
}
