package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;

class PolicySnapshotTest {

    @Test
    @DisplayName("성공: SCHEDULED 정책의 유효한 스냅샷 형식을 검증한다")
    void validateScheduledSuccess() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("09:00")
                .endTime("18:00")
                .build();

        // when & then
        snapshot.validate(PolicyType.SCHEDULED); // 예외 발생 안 함
    }

    @Test
    @DisplayName("실패: SCHEDULED 정책인데 durationMinutes가 포함되면 예외가 발생한다")
    void validateScheduledFailUnnecessaryField() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("09:00")
                .endTime("18:00")
                .durationMinutes(60) // SCHEDULED에는 불필요
                .build();

        // when & then
        assertThatThrownBy(() -> snapshot.validate(PolicyType.SCHEDULED))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.UNNECESSARY_SNAPSHOT_FIELD);
    }

    @Test
    @DisplayName("실패: SCHEDULED 정책인데 요일에 중복이 있으면 예외가 발생한다")
    void validateScheduledFailDuplicateDays() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY))
                .startTime("09:00")
                .endTime("18:00")
                .build();

        // when & then
        assertThatThrownBy(() -> snapshot.validate(PolicyType.SCHEDULED))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.DUPLICATE_DAY_OF_WEEK);
    }

    @Test
    @DisplayName("실패: 시간 형식이 올바르지 않으면 예외가 발생한다 (HH:mm)")
    void validateFailInvalidTimeFormat() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .startTime("25:00") // 잘못된 시간
                .endTime("12:60")   // 잘못된 분
                .build();

        // when & then
        assertThatThrownBy(() -> snapshot.validate(PolicyType.SCHEDULED))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.INVALID_TIME_FORMAT);
    }

    @Test
    @DisplayName("성공: ONCE 정책의 유효한 스냅샷 형식을 검증한다 (duration 사용)")
    void validateOnceSuccess() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .durationMinutes(30)
                .build();

        // when & then
        snapshot.validate(PolicyType.ONCE);
    }

    @Test
    @DisplayName("실패: ONCE 정책인데 요일(days) 정보가 포함되면 예외가 발생한다")
    void validateOnceFailUnnecessaryField() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .durationMinutes(30)
                .days(List.of(DayOfWeek.MONDAY))
                .build();

        // when & then
        assertThatThrownBy(() -> snapshot.validate(PolicyType.ONCE))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.UNNECESSARY_SNAPSHOT_FIELD);
    }

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
}
