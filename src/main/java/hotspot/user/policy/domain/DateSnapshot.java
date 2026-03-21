package hotspot.user.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정책 적용 시점의 전체 정보를 담는 스냅샷 도메인
 * JSON 구조: {"policyName": "...", "policyType": "...", "data": {...}}
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateSnapshot {

    private String policyName;

    private PolicyType policyType;

    private PolicySnapshot data; // 세부 시간 설정 (요일, 시간 등)
}
