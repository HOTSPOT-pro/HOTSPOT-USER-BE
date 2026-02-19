package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

/**
 * 가족-회선 매핑 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FamilySubscriptionRepositoryImplTest {

    @Mock
    private FamilySubscriptionJpaRepository familySubscriptionJpaRepository;

    @InjectMocks
    private FamilySubscriptionRepositoryImpl familySubscriptionRepository;

    @Test
    @DisplayName("회선 ID로 가족 결합 정보 조회 성공")
    void findBySubIdSuccess() {
        // given
        Long subId = 100L;
        SubscriptionEntity subEntity = SubscriptionEntity.builder()
                .subId(subId)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .phoneEnc("enc")
                .phoneHash("hash")
                .build();

        FamilySubscriptionEntity entity = FamilySubscriptionEntity.builder()
                .familySubId(1L)
                .subscription(subEntity)
                .family(FamilyEntity.builder().familyId(1L).build())
                .familyRole(FamilyRole.CHILD)
                .priority(-1)
                .build();

        given(familySubscriptionJpaRepository.findBySubscriptionSubId(subId)).willReturn(Optional.of(entity));

        // when
        Optional<FamilySubscription> result = familySubscriptionRepository.findBySubId(subId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFamilyRole()).isEqualTo(FamilyRole.CHILD);
    }
}
