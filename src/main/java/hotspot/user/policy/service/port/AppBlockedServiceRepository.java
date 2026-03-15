package hotspot.user.policy.service.port;

import java.util.List;
import java.util.Set;

import hotspot.user.policy.domain.AppBlockedService;

/**
 * 관리자 차단 앱 서비스 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface AppBlockedServiceRepository {
    List<AppBlockedService> findAll();
    long countByIdIn(Set<Long> ids);
    List<AppBlockedService> findAllActiveAndInDeleteByAppBlockedServiceIds(List<Long> ids);
    List<AppBlockedService> findAllByAppBlockedServiceIds(List<Long> ids);
}
