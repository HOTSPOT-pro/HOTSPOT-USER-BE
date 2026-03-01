package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlockPolicyTest {

    @Test
    @DisplayName("성공: 정책 상태 변경 시 isActive 필드가 업데이트된다")
    void updateIsActiveSuccess() {
        // given
        BlockPolicy policy = BlockPolicy.builder()
                .id(1L)
                .name("정책1")
                .isActive(false)
                .build();

        // when
        policy.updateIsActive(true);

        // then
        assertThat(policy.isActive()).isTrue();
    }

    @Test
    @DisplayName("성공: 요청자 가족 ID가 자신의 가족 ID와 같으면 접근을 허용한다")
    void isAllowedToSuccessSameFamily() {
        // given
        Long familyId = 100L;
        BlockPolicy policy = BlockPolicy.builder()
                .id(1L)
                .familyId(familyId)
                .build();

        // when
        boolean result = policy.isAllowedTo(familyId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공: 관리자 정책(familyId=null)은 모든 가족의 접근을 허용한다")
    void isAllowedToSuccessAdminPolicy() {
        // given
        BlockPolicy policy = BlockPolicy.builder()
                .id(1L)
                .familyId(null)
                .build();

        // when
        boolean result = policy.isAllowedTo(100L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("실패: 요청자 가족 ID가 정책의 가족 ID와 다르면 접근을 거부한다")
    void isAllowedToFailDifferentFamily() {
        // given
        BlockPolicy policy = BlockPolicy.builder()
                .id(1L)
                .familyId(100L)
                .build();

        // when
        boolean result = policy.isAllowedTo(200L);

        // then
        assertThat(result).isFalse();
    }
}
