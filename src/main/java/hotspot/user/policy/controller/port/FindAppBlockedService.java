package hotspot.user.policy.controller.port;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockPolicy;

import java.util.List;

/**
 * 관리자가 생성한 정책 조회
 */
public interface FindAppBlockedServiceService {
    List<AppBlockedService> findAll();
}
