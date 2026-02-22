package hotspot.user.family.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilyTest {

    @Test
    @DisplayName("가족의 우선순위 정책 타입을 업데이트할 수 있다")
    void updatePriorityType() {
        // given
        Family family = Family.builder()
                .id(1L)
                .priorityType(PriorityType.FIFO)
                .build();

        // when
        family.updatePriorityType(PriorityType.PRIORITY);

        // then
        assertThat(family.getPriorityType()).isEqualTo(PriorityType.PRIORITY);
    }
}
