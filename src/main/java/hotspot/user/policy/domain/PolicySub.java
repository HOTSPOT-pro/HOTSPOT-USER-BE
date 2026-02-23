package hotspot.user.policy.domain;

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
    private final Long subId;
    private final DateSnapshot dateSnapshot;
    private boolean isDeleted;

    // 논리 삭제 메서드
    public void delete() {
        isDeleted = true;
    }
}
