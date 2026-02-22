package hotspot.user.policy.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PolicySubTest {
    @Test
    @DisplayName("성공: PolicySub 논리 삭제 시 isDeleted 필드가 true가 된다")
    void deleteSuccess() {
        // given
        PolicySub policySub = PolicySub.builder()
                .id(1L)
                .isDeleted(false)
                .build();

        // when
        policySub.delete();

        // then
        assertThat(policySub.getIsDeleted()).isTrue();
    }
}
