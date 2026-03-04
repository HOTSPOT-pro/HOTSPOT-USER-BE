package hotspot.user.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자가 생성한 앱 차단 서비스 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class AppBlockedService {
    private final Long id;
    private final String name;
    private final String serviceCode;
    private final boolean isActive;
}
