package hotspot.user.policy.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.outbox.consistencyOutbox.domain.PolicyPayload;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;

class PolicySnapshotUtilTest {

    @Test
    @DisplayName("SCHEDULED 정책은 요일과 시간으로 encoded 문자열 생성")
    void scheduledPolicyEncodeSuccess() {

        PolicySnapshot snapshot =
                PolicySnapshot.builder()
                        .days(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                        .startTime("09:00")
                        .endTime("18:00")
                        .build();

        PolicyPayload payload =
                PolicySnapshotUtil.map(
                        1L,
                        PolicyType.SCHEDULED,
                        snapshot
                );

        assertThat(payload.policyId()).isEqualTo(1L);
        assertThat(payload.encoded()).isEqualTo("1,3|09:00|18:00");
        assertThat(payload.expireEpoch()).isNull();
    }

    @Test
    @DisplayName("ONCE 정책 - durationMinutes 기반 expireEpoch 생성")
    void oncePolicyDurationSuccess() {

        PolicySnapshot snapshot =
                PolicySnapshot.builder()
                        .durationMinutes(60)
                        .build();

        long before =
                Instant.now()
                        .plusSeconds(59 * 60)
                        .getEpochSecond();

        PolicyPayload payload =
                PolicySnapshotUtil.map(
                        2L,
                        PolicyType.ONCE,
                        snapshot
                );

        long after =
                Instant.now()
                        .plusSeconds(61 * 60)
                        .getEpochSecond();

        assertThat(payload.policyId()).isEqualTo(2L);
        assertThat(payload.encoded()).isNull();
        assertThat(payload.expireEpoch()).isBetween(before, after);
    }

    @Test
    @DisplayName("ONCE 정책 - endTime 기반 expireEpoch 생성")
    void oncePolicyEndTimeSuccess() {

        PolicySnapshot snapshot =
                PolicySnapshot.builder()
                        .endTime("23:59")
                        .build();

        PolicyPayload payload =
                PolicySnapshotUtil.map(
                        3L,
                        PolicyType.ONCE,
                        snapshot
                );

        assertThat(payload.policyId()).isEqualTo(3L);
        assertThat(payload.encoded()).isNull();
        assertThat(payload.expireEpoch()).isNotNull();
    }

    @Test
    @DisplayName("ONCE 정책 snapshot이 잘못되면 예외 발생")
    void oncePolicyInvalidSnapshot() {

        PolicySnapshot snapshot =
                PolicySnapshot.builder()
                        .build();

        assertThatThrownBy(() ->
                PolicySnapshotUtil.map(
                        4L,
                        PolicyType.ONCE,
                        snapshot
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid ONCE policy snapshot");
    }

}
