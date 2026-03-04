package hotspot.user.policy.service.port;

import java.util.List;
import java.util.Set;

import hotspot.user.policy.domain.BlockedServiceSub;

/**
 * 앱 차단 서비스 - 회선 매핑 테이블 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface BlockedServiceSubRepository {
    List<BlockedServiceSub> findBySubId(Long subId);
    List<BlockedServiceSub> findActiveBySubId(Long subId);
    List<Long> findActiveServiceIdsBySubId(Long subId); // 활성화된 앱 차단 서비스 id만 리턴
    List<BlockedServiceSub> saveAll(List<BlockedServiceSub> blockedServiceSubList);
}
