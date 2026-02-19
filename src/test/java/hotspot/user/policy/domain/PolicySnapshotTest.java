package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolicySnapshotTest {

    @Test
    @DisplayName("SCHEDULED 정책 여부 확인: 요일과 시작/종료 시간 문자열이 있으면 true를 반환한다")
    void isScheduledPolicySuccess() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                .startTime("09:00")
                .endTime("18:00")
                .build();

        // when & then
        assertThat(snapshot.isScheduledPolicy()).isTrue();
        assertThat(snapshot.getStartLocalTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(snapshot.getEndLocalTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    @DisplayName("ONCE 정책 여부 확인: 지속 시간(minutes)이 0보다 크거나 시간 범위가 있으면 true를 반환한다")
    void isOncePolicySuccess() {
        // given: durationMinutes 사용
        PolicySnapshot snapshot1 = PolicySnapshot.builder()
                .durationMinutes(60)
                .build();

        // given: startTime/endTime 사용
        PolicySnapshot snapshot2 = PolicySnapshot.builder()
                .startTime("06:00")
                .endTime("23:59")
                .build();

        // when & then
        assertThat(snapshot1.isOncePolicy()).isTrue();
        assertThat(snapshot2.isOncePolicy()).isTrue();
    }

    @Test
    @DisplayName("필수 값이 누락된 경우 정책 유형 확인 메서드는 false를 반환한다")
    void policyValidationFail() {
        // given
        PolicySnapshot emptySnapshot = new PolicySnapshot();
        PolicySnapshot invalidScheduled = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("09:00")
                // endTime 누락
                .build();

        // when & then
        assertThat(emptySnapshot.isScheduledPolicy()).isFalse();
        assertThat(emptySnapshot.isOncePolicy()).isFalse();
        assertThat(invalidScheduled.isScheduledPolicy()).isFalse();
    }
}
