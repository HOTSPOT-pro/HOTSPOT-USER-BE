package hotspot.user.family.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;

/**
 * FamilySubscription 도메인 단위 테스트
 */
class FamilySubscriptionTest {

    @Test
    @DisplayName("데이터 한도를 업데이트할 수 있다")
    void updateDataLimit() {
        // given
        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(1L)
                .dataLimit(100)
                .build();

        // when
        familySubscription.updateDataLimit(500);

        // then
        assertThat(familySubscription.getDataLimit()).isEqualTo(500);
    }

    @Test
    @DisplayName("데이터 한도가 -1 미만인 경우 예외가 발생한다")
    void updateDataLimitFail() {
        // given
        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(1L)
                .dataLimit(100)
                .build();

        // when & then
        assertThatThrownBy(() -> familySubscription.updateDataLimit(-2))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.INVALID_DATA_LIMIT);
    }

    @Test
    @DisplayName("우선순위를 업데이트할 수 있다")
    void updatePriority() {
        // given
        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(1L)
                .priority(1)
                .build();

        // when
        familySubscription.updatePriority(5);

        // then
        assertThat(familySubscription.getPriority()).isEqualTo(5);
    }

    @Test
    @DisplayName("가족 내 역할을 업데이트할 수 있다")
    void updateFamilyRole() {
        // given
        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(1L)
                .familyRole(FamilyRole.CHILD)
                .build();

        // when
        familySubscription.updateFamilyRole(FamilyRole.PARENT);

        // then
        assertThat(familySubscription.getFamilyRole()).isEqualTo(FamilyRole.PARENT);
    }

    @Test
    @DisplayName("같은 가족 구성원인지 검증에 성공한다")
    void validateSameFamilySuccess() {
        // given
        Family family = Family.builder().id(10L).build();
        FamilySubscription provider = FamilySubscription.builder()
                .family(family)
                .build();
        FamilySubscription target = FamilySubscription.builder()
                .family(family)
                .build();

        // when & then
        provider.validateSameFamily(target);
    }

    @Test
    @DisplayName("다른 가족 구성원인 경우 검증 시 예외가 발생한다")
    void validateSameFamilyFail() {
        // given
        Family family1 = Family.builder().id(10L).build();
        Family family2 = Family.builder().id(20L).build();

        FamilySubscription provider = FamilySubscription.builder()
                .family(family1)
                .build();
        FamilySubscription target = FamilySubscription.builder()
                .family(family2)
                .build();

        // when & then
        assertThatThrownBy(() -> provider.validateSameFamily(target))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.NOT_FAMILY_MEMBER);
    }

    @Test
    @DisplayName("FamilySubscription 도메인 객체를 생성할 수 있다")
    void createFamilySubscription() {
        // given
        Subscription subscription = Subscription.builder().id(1L).build();
        Family family = Family.builder().id(1L).build();

        // when
        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(1L)
                .subscription(subscription)
                .family(family)
                .familyRole(FamilyRole.OWNER)
                .priority(1)
                .dataLimit(1000)
                .build();

        // then
        assertThat(familySubscription.getId()).isEqualTo(1L);
        assertThat(familySubscription.getFamilyRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(familySubscription.getDataLimit()).isEqualTo(1000);
    }
}
