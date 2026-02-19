package hotspot.user.policy.service.port;

import hotspot.user.policy.domain.BlockedServiceSub;

import java.util.List;

/**
 * 앱 차단 서비스 - 회선 매핑 테이블 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface BlockedServiceSubRepository {
    List<BlockedServiceSub> findBySubId(Long subId);
}
