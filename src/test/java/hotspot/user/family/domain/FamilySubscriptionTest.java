package hotspot.user.family.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
