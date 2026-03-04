package hotspot.user.policy.domain;

import hotspot.user.subscription.domain.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자가 생성한 차단 정책 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class BlockedServiceSub {
    private final Long id;
    private final Subscription subscription;
    private final AppBlockedService appBlockedService;
    private boolean isActive;

    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }

}
