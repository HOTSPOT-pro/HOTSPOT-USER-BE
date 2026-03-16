package hotspot.user.policy.domain;

import java.time.LocalTime;

/**
 * 시간 구간을 나타내는 도메인 객체
 */
public record TimeRange(LocalTime start, LocalTime end) {

    public TimeRange {
        if (start == null || end == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 필수입니다.");
        }
    }

    /**
     * 다른 구간과 겹치거나 맞닿아 있는지 확인
     */
    public boolean overlapsOrAbuts(TimeRange other) {
        return !this.start.isAfter(other.end) && !other.start.isAfter(this.end);
    }

    /**
     * 두 구간을 하나로 병합 (두 구간이 겹치거나 맞닿아 있다고 가정)
     */
    public TimeRange merge(TimeRange other) {
        LocalTime newStart = this.start.isBefore(other.start) ? this.start : other.start;
        LocalTime newEnd = this.end.isAfter(other.end) ? this.end : other.end;
        return new TimeRange(newStart, newEnd);
    }
}
