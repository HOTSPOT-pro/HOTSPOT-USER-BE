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
public class BlockPolicy {
    private final Long id;
    private final String name;
    private final Long familyId;
    private final PolicyType policyType;
    private PolicySnapshot policySnapshot;
    private String policyDescription;
    private boolean isActive;
    private boolean isDeleted;

    /**
     * 해당 가족이 이 정책에 접근하거나 적용할 권한이 있는지 확인
     * @param requesterFamilyId 요청자의 가족 ID
     * @return 관리자 정책(null)이거나 본인 가족 정책이면 true
     */
    public boolean isAllowedTo(Long requesterFamilyId) {
        return this.familyId == null || this.familyId.equals(requesterFamilyId);
    }

}
