package hotspot.user.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자가 생성한 차단 정책 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class PolicySub {
    private final Long id;
    private final Long subId;
    private final PolicySnapshot policySnapshot;

}
