package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 정책 상세 정보 도메인 단위 테스트 코드
 */

class PolicySnapshotTest {

    @Test
    @DisplayName("SCHEDULED 정책 여부 확인: 요일과 시작/종료 시간이 있으면 true를 반환한다")
    void isScheduledPolicySuccess() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        // when & then
        assertThat(snapshot.isScheduledPolicy()).isTrue();
        assertThat(snapshot.isOncePolicy()).isFalse();
    }

    @Test
    @DisplayName("ONCE 정책 여부 확인: 지속 시간(minutes)이 0보다 크면 true를 반환한다")
    void isOncePolicySuccess() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .durationMinutes(60)
                .build();

        // when & then
        assertThat(snapshot.isOncePolicy()).isTrue();
        assertThat(snapshot.isScheduledPolicy()).isFalse();
    }

    @Test
    @DisplayName("필수 값이 누락된 경우 정책 유형 확인 메서드는 false를 반환한다")
    void policyValidationFail() {
        // given
        PolicySnapshot emptySnapshot = new PolicySnapshot();
        PolicySnapshot invalidScheduled = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime(LocalTime.of(9, 0))
                // endTime 누락
                .build();

        // when & then
        assertThat(emptySnapshot.isScheduledPolicy()).isFalse();
        assertThat(emptySnapshot.isOncePolicy()).isFalse();
        assertThat(invalidScheduled.isScheduledPolicy()).isFalse();
    }
}
