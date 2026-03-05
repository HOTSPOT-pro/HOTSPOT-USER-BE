package hotspot.user.policy.domain;

import java.time.LocalDateTime;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;
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

    // 정책 상태 업데이트
    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 일회성 정책(ONCE)이 현재 시간 기준으로 만료되었는지 확인한다.
     * @param modifiedTime 정책이 마지막으로 수정(활성화)된 시각 (PolicySub.modifiedTime)
     * @return 만료되었으면 true
     */
    public boolean isOnceExpired(LocalDateTime modifiedTime) {
        if (this.policyType != PolicyType.ONCE || modifiedTime == null || this.policySnapshot == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 지속 시간(Minutes) 기준 만료 체크 (마지막 수정 시점부터 N분)
        if (this.policySnapshot.getDurationMinutes() != null) {
            return now.isAfter(modifiedTime.plusMinutes(this.policySnapshot.getDurationMinutes()));
        }

        // 2. 명시적 종료 시간(endTime) 기준 만료 체크
        if (this.policySnapshot.getEndLocalTime() != null) {
            // 적용된 날짜의 종료 시간으로 계산
            LocalDateTime expirationTime = modifiedTime.toLocalDate()
                    .atTime(this.policySnapshot.getEndLocalTime());

            // 만약 종료 시간이 시작 시간보다 빨라 다음 날인 경우 처리
            if (this.policySnapshot.getStartLocalTime() != null
                    && this.policySnapshot.getEndLocalTime().isBefore(this.policySnapshot.getStartLocalTime())) {
                expirationTime = expirationTime.plusDays(1);
            }

            return now.isAfter(expirationTime);
        }

        return false;
    }

    public BlockPolicy update(String name, String description, PolicyType policyType,
                              PolicySnapshot snapshot, Boolean isActive) {
        if (this.isDeleted) {
            throw new ApplicationException(PolicyErrorCode.ALREADY_DELETED_POLICY);
        }

        return BlockPolicy.builder()
                .id(this.id)
                .familyId(this.familyId)
                .policyType(policyType != null ? policyType : this.policyType)
                .name(name != null ? name : this.name)
                .policyDescription(description != null ? description : this.policyDescription)
                .policySnapshot(snapshot != null ? snapshot : this.policySnapshot)
                .isActive(isActive != null ? isActive : this.isActive)
                .isDeleted(this.isDeleted)
                .build();
    }

}
