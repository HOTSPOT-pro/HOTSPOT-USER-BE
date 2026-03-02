package hotspot.user.policy.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정책 스냅샷 클래스
 * LocalTime 역직렬화 이슈 해결을 위해 시간 필드를 String으로 관리한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicySnapshot {

    // SCHEDULED 정책은 요일이 필수
    private List<DayOfWeek> days;

    private String startTime; // "HH:mm" 형식 문자열

    private String endTime;   // "HH:mm" 형식 문자열

    // ONCE 정책에서 주로 사용
    private Integer durationMinutes;

    // 문자열 시작 시간을 LocalTime으로 변환하여 반환
    @JsonIgnore
    public LocalTime getStartLocalTime() {
        return startTime != null ? LocalTime.parse(startTime) : null;
    }

    // 문자열 종료 시간을 LocalTime으로 변환하여 반환
    @JsonIgnore
    public LocalTime getEndLocalTime() {
        return endTime != null ? LocalTime.parse(endTime) : null;
    }

    // SCHEDULED 정책 유효성 확인: 요일과 시작/종료 시간이 모두 있어야 함
    @JsonIgnore
    public boolean isScheduledPolicy() {
        return days != null && !days.isEmpty() && startTime != null && endTime != null;
    }

    // ONCE 정책 유효성 확인: 지속 시간이 있거나, 혹은 시작/종료 시간이 있어야 함
    @JsonIgnore
    public boolean isOncePolicy() {
        return (durationMinutes != null && durationMinutes > 0) || (startTime != null && endTime != null);
    }

    @JsonIgnore
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01][0-9]|2[0-3]):[0-5][0-9]$");

    // 정책 타입에 따른 유효성 검증 (강화된 로직)
    public void validate(PolicyType type) {
        // 1. 시간 형식 검증 (HH:mm)
        validateTimeFormat(startTime);
        validateTimeFormat(endTime);

        // 2. 타입별 불필요 필드 및 필수 필드 검증
        if (type == PolicyType.SCHEDULED) {
            if (durationMinutes != null) {
                throw new ApplicationException(PolicyErrorCode.UNNECESSARY_SNAPSHOT_FIELD);
            }
            validateScheduledPolicy();
        } else if (type == PolicyType.ONCE) {
            if (days != null && !days.isEmpty()) {
                throw new ApplicationException(PolicyErrorCode.UNNECESSARY_SNAPSHOT_FIELD);
            }
            validateOncePolicy();
        }
    }

    private void validateTimeFormat(String time) {
        if (time != null && !TIME_PATTERN.matcher(time).matches()) {
            throw new ApplicationException(PolicyErrorCode.INVALID_TIME_FORMAT);
        }
    }

    private void validateScheduledPolicy() {
        // 요일 중복 체크
        if (days != null && !days.isEmpty()) {
            Set<DayOfWeek> uniqueDays = new HashSet<>(days);
            if (uniqueDays.size() != days.size()) {
                throw new ApplicationException(PolicyErrorCode.DUPLICATE_DAY_OF_WEEK);
            }
        }

        if (!isScheduledPolicy()) {
            throw new ApplicationException(PolicyErrorCode.INVALID_POLICY_FORMAT);
        }
    }

    private void validateOncePolicy() {
        if (!isOncePolicy()) {
            throw new ApplicationException(PolicyErrorCode.INVALID_POLICY_FORMAT);
        }
    }
}
