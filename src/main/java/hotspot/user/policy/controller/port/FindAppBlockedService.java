package hotspot.user.policy.controller.port;

import java.util.List;

import hotspot.user.policy.controller.response.AppBlockedServiceResponse;

/**
 * 관리자가 생성한 앱 차단 서비스 조회
 */
public interface FindAppBlockedService {
    List<AppBlockedServiceResponse> findAll();
}
