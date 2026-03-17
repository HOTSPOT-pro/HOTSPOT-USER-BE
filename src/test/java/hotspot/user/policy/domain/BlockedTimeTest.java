package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlockedTimeTest {

    @Test
    @DisplayName("성공: 일반적인 시간 정책을 추가하고 병합한다")
    void addAndMergeSuccess() {
        BlockedTime blockedTime = new BlockedTime();
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("10:00")
                .endTime("12:00")
                .build();
        
        // 같은 요일에 겹치는 정책 추가
        PolicySnapshot snapshot2 = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("11:00")
                .endTime("13:00")
                .build();

        blockedTime.addPolicy(snapshot, PolicyType.SCHEDULED);
        blockedTime.addPolicy(snapshot2, PolicyType.SCHEDULED);
        blockedTime.mergeAll();

        List<TimeRange> mondayRanges = blockedTime.getDayRanges().get(DayOfWeek.MONDAY);
        assertThat(mondayRanges).hasSize(1);
        assertThat(mondayRanges.get(0).start()).isEqualTo(LocalTime.of(10, 0));
        assertThat(mondayRanges.get(0).end()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("성공: 자정을 넘기는 정책은 다음날로 분리되어 저장된다")
    void addPolicyWithMidnightSplit() {
        BlockedTime blockedTime = new BlockedTime();
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("22:00")
                .endTime("02:00") // 다음날 새벽 2시
                .build();

        blockedTime.addPolicy(snapshot, PolicyType.SCHEDULED);
        blockedTime.mergeAll();

        // 월요일: 22:00 ~ 23:59:59.999...
        assertThat(blockedTime.getDayRanges().get(DayOfWeek.MONDAY)).hasSize(1);
        assertThat(blockedTime.getDayRanges().get(DayOfWeek.MONDAY).get(0).start()).isEqualTo(LocalTime.of(22, 0));

        // 화요일: 00:00 ~ 02:00
        assertThat(blockedTime.getDayRanges().get(DayOfWeek.TUESDAY)).hasSize(1);
        assertThat(blockedTime.getDayRanges().get(DayOfWeek.TUESDAY).get(0).start()).isEqualTo(LocalTime.MIN);
        assertThat(blockedTime.getDayRanges().get(DayOfWeek.TUESDAY).get(0).end()).isEqualTo(LocalTime.of(2, 0));
    }

    @Test
    @DisplayName("성공: 일회성(ONCE) 정책은 오늘 요일에만 적용된다")
    void addOncePolicySuccess() {
        BlockedTime blockedTime = new BlockedTime();
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .startTime("15:00")
                .endTime("16:00")
                .build();

        blockedTime.addPolicy(snapshot, PolicyType.ONCE);
        
        DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        assertThat(blockedTime.getDayRanges().get(today)).isNotEmpty();
    }

    @Test
    @DisplayName("성공: 스냅샷이 null이거나 리스트가 비어있을 때 안전하게 동작한다")
    void addPolicyWithNullOrEmpty() {
        BlockedTime blockedTime = new BlockedTime();
        
        // null 스냅샷
        blockedTime.addPolicy(null, PolicyType.SCHEDULED);
        
        // 날짜 리스트가 null인 경우
        PolicySnapshot snapshot = PolicySnapshot.builder().build();
        blockedTime.addPolicy(snapshot, PolicyType.SCHEDULED);
        
        blockedTime.mergeAll();
        
        for (DayOfWeek day : DayOfWeek.values()) {
            assertThat(blockedTime.getDayRanges().get(day)).isEmpty();
        }
    }
}
