package hotspot.user.policy.domain;

import hotspot.user.subscription.domain.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 정책 - 회선 매핑 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class PolicySub {
    private final Long id;
    private final Long policyId;
    private final Subscription subscription;
    private final DateSnapshot dateSnapshot;
}
