package hotspot.user.family.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.subscription.domain.Subscription;

class FamilySubscriptionsTest {

    @Test
    @DisplayName("FIFO 모드로 업데이트 시 모든 구성원의 순위가 -1이 된다")
    void updateAllToFifo() {
        // given
        FamilySubscription sub1 = createFamilySubscription(100L, 1);
        FamilySubscription sub2 = createFamilySubscription(101L, 2);
        FamilySubscriptions members = new FamilySubscriptions(List.of(sub1, sub2));

        // when
        members.updateAllToFifo();

        // then
        assertThat(sub1.getPriority()).isEqualTo(-1);
        assertThat(sub2.getPriority()).isEqualTo(-1);
    }

    @Test
    @DisplayName("정상적인 우선순위 Map이 주어지면 구성원들의 순위가 업데이트된다")
    void updatePrioritiesSuccess() {
        // given
        FamilySubscription sub1 = createFamilySubscription(100L, -1);
        FamilySubscription sub2 = createFamilySubscription(101L, -1);
        FamilySubscriptions members = new FamilySubscriptions(List.of(sub1, sub2));

        Map<Long, Integer> newPriorityMap = Map.of(
                100L, 2,
                101L, 1
        );

        // when
        members.updatePriorities(newPriorityMap);

        // then
        assertThat(sub1.getPriority()).isEqualTo(2);
        assertThat(sub2.getPriority()).isEqualTo(1);
    }

    @Test
    @DisplayName("우선순위 업데이트 시 모든 구성원의 값이 포함되어 있지 않으면 예외가 발생한다")
    void updatePrioritiesFailMissingValues() {
        // given
        FamilySubscription sub1 = createFamilySubscription(100L, -1);
        FamilySubscription sub2 = createFamilySubscription(101L, -1);
        FamilySubscriptions members = new FamilySubscriptions(List.of(sub1, sub2));

        Map<Long, Integer> incompleteMap = Map.of(100L, 1); // 101L 누락

        // when & then
        assertThatThrownBy(() -> members.updatePriorities(incompleteMap))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.MISSING_PRIORITY_VALUES.getMessage());
    }

    @Test
    @DisplayName("우선순위 값에 중복이 있으면 예외가 발생한다")
    void updatePrioritiesFailDuplicate() {
        // given
        FamilySubscription sub1 = createFamilySubscription(100L, -1);
        FamilySubscription sub2 = createFamilySubscription(101L, -1);
        FamilySubscriptions members = new FamilySubscriptions(List.of(sub1, sub2));

        Map<Long, Integer> duplicateMap = Map.of(
                100L, 1,
                101L, 1 // 중복
        );

        // when & then
        assertThatThrownBy(() -> members.updatePriorities(duplicateMap))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.DUPLICATE_PRIORITY.getMessage());
    }

    @Test
    @DisplayName("우선순위 값이 1부터 시작하는 연속된 숫자가 아니면 예외가 발생한다")
    void updatePrioritiesFailNotContinuous() {
        // given
        FamilySubscription sub1 = createFamilySubscription(100L, -1);
        FamilySubscription sub2 = createFamilySubscription(101L, -1);
        FamilySubscriptions members = new FamilySubscriptions(List.of(sub1, sub2));

        Map<Long, Integer> nonContinuousMap = Map.of(
                100L, 1,
                101L, 3 // 2가 빠짐
        );

        // when & then
        assertThatThrownBy(() -> members.updatePriorities(nonContinuousMap))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_CONTINUOUS_PRIORITY.getMessage());
    }

    private FamilySubscription createFamilySubscription(Long subId, int priority) {
        return FamilySubscription.builder()
                .subscription(Subscription.builder().id(subId).build())
                .priority(priority)
                .build();
    }
}
