package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeRangeTest {

    @Test
    @DisplayName("성공: 두 시간 구간이 겹치거나 맞닿아 있는지 확인한다")
    void overlapsOrAbutsSuccess() {
        TimeRange base = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));

        // 겹치는 경우
        assertThat(base.overlapsOrAbuts(new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0)))).isTrue();
        // 맞닿아 있는 경우
        assertThat(base.overlapsOrAbuts(new TimeRange(LocalTime.of(12, 0), LocalTime.of(13, 0)))).isTrue();
        // 완전히 포함되는 경우
        assertThat(base.overlapsOrAbuts(new TimeRange(LocalTime.of(10, 30), LocalTime.of(11, 30)))).isTrue();
        // 겹치지 않는 경우
        assertThat(base.overlapsOrAbuts(new TimeRange(LocalTime.of(13, 0), LocalTime.of(14, 0)))).isFalse();
    }

    @Test
    @DisplayName("성공: 두 시간 구간을 하나로 병합한다")
    void mergeSuccess() {
        TimeRange range1 = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
        TimeRange range2 = new TimeRange(LocalTime.of(11, 0), LocalTime.of(13, 0));

        TimeRange merged = range1.merge(range2);

        assertThat(merged.start()).isEqualTo(LocalTime.of(10, 0));
        assertThat(merged.end()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    @DisplayName("실패: 시작 또는 종료 시간이 null이면 예외가 발생한다")
    void constructorFailByNull() {
        assertThatThrownBy(() -> new TimeRange(null, LocalTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
