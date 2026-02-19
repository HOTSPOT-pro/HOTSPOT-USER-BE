package hotspot.user.policy.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * 정책 스냅샷 클래스
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 값이 null인 필드는 JSON에서 생략
public class PolicySnapshot {

    // PolicyType.SCHEDULED만 해당
    private List<DayOfWeek> days;

    @JsonFormat(pattern = "HH:mm") // "00:00" 형식을 LocalTime으로 자동 변환
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    // PolicyType.ONCE만 해당
    private Integer durationMinutes;

    // SCHEDULED 정책은 요일, 시간이 필수임
    public boolean isScheduledPolicy() {
        return days != null && !days.isEmpty() && startTime != null && endTime != null;
    }

    // ONCE 정책은 지속 시간이 필수임
    public boolean isOncePolicy() {
        return durationMinutes != null && durationMinutes > 0;
    }
}
