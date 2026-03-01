package hotspot.user.policy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;

class BlockPolicyTest {

    @Test
    @DisplayName("성공: 정책 정보 수정 시 요청된 필드만 업데이트된다 (PATCH 방식)")
    void updateSuccess() {
        // given
        BlockPolicy policy = BlockPolicy.builder()
                .id(1L)
                .name("기본 이름")
                .policyDescription("기본 설명")
                .policyType(PolicyType.SCHEDULED)
                .isActive(true)
                .build();

        // when: 이름과 설명만 수정
        BlockPolicy updated = policy.update("수정된 이름", "수정된 설명", null, null, null);

        // then
        assertThat(updated.getName()).isEqualTo("수정된 이름");
        assertThat(updated.getPolicyDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getPolicyType()).isEqualTo(PolicyType.SCHEDULED); // 기존 유지
        assertThat(updated.isActive()).isTrue(); // 기존 유지
    }

    @Test
    @DisplayName("실패: 이미 삭제된 정책은 수정하려고 시도하면 예외가 발생한다")
    void updateFailAlreadyDeleted() {
        // given
        BlockPolicy deletedPolicy = BlockPolicy.builder()
                .id(1L)
                .isDeleted(true)
                .build();

        // when & then
        assertThatThrownBy(() -> deletedPolicy.update("이름", null, null, null, null))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.ALREADY_DELETED_POLICY);
    }

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
