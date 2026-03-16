package hotspot.user.policy.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

/**
 * 사용자별 요일별 차단 시간대를 관리하는 도메인 객체
 */
@Getter
public class BlockedTime {
    private final Map<DayOfWeek, List<TimeRange>> dayRanges = new EnumMap<>(DayOfWeek.class);

    public BlockedTime() {
        for (DayOfWeek day : DayOfWeek.values()) {
            dayRanges.put(day, new ArrayList<>());
        }
    }

    /**
     * 정책 스냅샷을 기반으로 차단 시간대 추가 (자정 처리 포함)
     */
    public void addPolicy(PolicySnapshot snapshot, PolicyType type) {
        if (snapshot == null) {
            return;
        }

        if (type == PolicyType.SCHEDULED && snapshot.getDays() != null) {
            for (DayOfWeek day : snapshot.getDays()) {
                addRangeWithMidnightSplit(day, snapshot.getStartLocalTime(), snapshot.getEndLocalTime());
            }
        } else if (type == PolicyType.ONCE) {
            // 일회성 정책은 현재 요일에만 적용
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            addRangeWithMidnightSplit(today, snapshot.getStartLocalTime(), snapshot.getEndLocalTime());
        }
    }

    /**
     * 자정 넘김 처리를 포함하여 시간 구간 추가
     */
    private void addRangeWithMidnightSplit(DayOfWeek day, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return;
        }

        if (end.isBefore(start)) {
            // 자정을 넘기는 경우: 당일(start~23:59)과 익일(00:00~end)로 분리
            dayRanges.get(day).add(new TimeRange(start, LocalTime.MAX));
            dayRanges.get(day.plus(1)).add(new TimeRange(LocalTime.MIN, end));
        } else {
            dayRanges.get(day).add(new TimeRange(start, end));
        }
    }

    /**
     * 각 요일별로 중첩된 시간 구간들을 병합
     */
    public void mergeAll() {
        for (DayOfWeek day : DayOfWeek.values()) {
            List<TimeRange> ranges = dayRanges.get(day);
            if (ranges.size() <= 1) {
                continue;
            }

            // 시작 시간 순 정렬
            ranges.sort(Comparator.comparing(TimeRange::start));

            List<TimeRange> merged = new ArrayList<>();
            TimeRange current = ranges.get(0);

            for (int i = 1; i < ranges.size(); i++) {
                TimeRange next = ranges.get(i);
                if (current.overlapsOrAbuts(next)) {
                    current = current.merge(next);
                } else {
                    merged.add(current);
                    current = next;
                }
            }
            merged.add(current);
            dayRanges.put(day, merged);
        }
    }
}
