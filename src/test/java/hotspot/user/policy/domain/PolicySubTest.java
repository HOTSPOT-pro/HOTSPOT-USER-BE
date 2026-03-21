package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolicySubTest {
    @Test
    @DisplayName("성공: PolicySub 상태 변경 시 isActive 필드가 업데이트된다")
    void updateIsActiveSuccess() {
        // given
        PolicySub policySub = PolicySub.builder()
                .id(1L)
                .isActive(true)
                .build();

        // when
        policySub.updateIsActive(false);

        // then
        assertThat(policySub.isActive()).isFalse();
    }
}
