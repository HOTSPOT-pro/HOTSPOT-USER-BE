package hotspot.user.policy.domain;

import java.time.LocalDateTime;

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
    private final Long subId;
    private final Long blockPolicyId;
    private boolean isActive;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    // 논리 삭제 메서드
    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 정책 규정(BlockPolicy)을 넘겨받아 만료 여부를 스스로 판단하고 비활성화한다.
     * @param policy 해당 매핑에 연결된 정책 정보
     * @return 상태가 비활성으로 변경되었다면 true
     */
    public boolean deactivateIfExpired(BlockPolicy policy) {
        if (policy == null || !this.isActive) {
            return false;
        }

        // 정책 ID 일치 여부 확인 (방어 코드)
        if (!policy.getId().equals(this.blockPolicyId)) {
            return false;
        }

        // 만료 판별 및 상태 전이
        if (policy.isOnceExpired(this.modifiedTime)) {
            this.isActive = false;
            return true;
        }

        return false;
    }
}
