package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.infrastructure.FamilySubscriptionJpaRepository.FamilySubDataLimitRow;
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.subscription.domain.Subscription;
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

    @Test
    @DisplayName("가족 ID로 소속된 모든 구성원 정보 조회 성공")
    void findByFamilyIdSuccess() {
        // given
        Long familyId = 1L;
        FamilySubscriptionEntity entity = FamilySubscriptionEntity.builder()
                .familySubId(1L)
                .subscription(SubscriptionEntity.builder().subId(100L)
                        .member(MemberEntity.builder().id(1L).build())
                        .plan(PlanEntity.builder().planId(1L).build())
                        .build())
                .family(FamilyEntity.builder().familyId(familyId).build())
                .familyRole(FamilyRole.CHILD)
                .build();

        given(familySubscriptionJpaRepository.findByFamilyFamilyId(familyId)).willReturn(List.of(entity));

        // when
        List<FamilySubscription> result = familySubscriptionRepository.findByFamilyId(familyId);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("멤버 ID로 소속된 가족 매핑 정보 조회 성공")
    void findByMemberIdSuccess() {
        // given
        Long memberId = 1L;
        FamilySubscriptionEntity entity = FamilySubscriptionEntity.builder()
                .familySubId(1L)
                .subscription(SubscriptionEntity.builder().subId(100L)
                        .member(MemberEntity.builder().id(memberId).build())
                        .plan(PlanEntity.builder().planId(1L).build())
                        .build())
                .family(FamilyEntity.builder().familyId(1L).build())
                .familyRole(FamilyRole.CHILD)
                .build();

        given(familySubscriptionJpaRepository.findBySubscriptionMemberId(memberId)).willReturn(Optional.of(entity));

        // when
        Optional<FamilySubscription> result = familySubscriptionRepository.findByMemberId(memberId);

        // then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("가족 결합 정보를 저장할 수 있다")
    void saveSuccess() {
        // given
        Subscription subscription = Subscription.builder()
                .id(100L)
                .build();

        Family family = Family.builder()
                .id(1L)
                .build();

        FamilySubscription domain = FamilySubscription.builder()
                .subscription(subscription)
                .family(family)
                .familyRole(FamilyRole.CHILD)
                .priority(-1)
                .dataLimit(500)
                .build();

        // entityToDomain 변환 시 필요한 연관 엔티티들 모킹
        SubscriptionEntity subEntity = SubscriptionEntity.builder()
                .subId(100L)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .phoneEnc("enc")
                .phoneHash("hash")
                .build();

        FamilyEntity familyEntity = FamilyEntity.builder()
                .familyId(1L)
                .build();

        FamilySubscriptionEntity entity = FamilySubscriptionEntity.builder()
                .familySubId(1L)
                .subscription(subEntity)
                .family(familyEntity)
                .familyRole(FamilyRole.CHILD)
                .dataLimit(500)
                .build();

        org.mockito.BDDMockito.given(familySubscriptionJpaRepository.save(
                org.mockito.ArgumentMatchers.any(FamilySubscriptionEntity.class))).willReturn(entity);

        // when
        FamilySubscription result = familySubscriptionRepository.save(domain);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDataLimit()).isEqualTo(500);
        org.mockito.Mockito.verify(familySubscriptionJpaRepository).save(
                org.mockito.ArgumentMatchers.any(FamilySubscriptionEntity.class));
    }

    @Test
    @DisplayName("여러 구성원의 우선순위를 일괄 업데이트할 수 있다")
    void updatePrioritiesSuccess() {
        // given
        FamilySubscription sub1 = FamilySubscription.builder()
                .subscription(Subscription.builder().id(100L).build())
                .priority(1)
                .build();
        FamilySubscription sub2 = FamilySubscription.builder()
                .subscription(Subscription.builder().id(101L).build())
                .priority(2)
                .build();

        // when
        familySubscriptionRepository.updatePriorities(List.of(sub1, sub2));

        // then
        org.mockito.Mockito.verify(familySubscriptionJpaRepository, org.mockito.Mockito.times(1))
                .updatePriority(100L, 1);
        org.mockito.Mockito.verify(familySubscriptionJpaRepository, org.mockito.Mockito.times(1))
                .updatePriority(101L, 2);
    }

    @Test
    @DisplayName("회선 ID로 구성원별 데이터 한도 조회 성공")
    void findDataLimitBySubIdSuccess() {
        // given
        Long subId = 100L;
        FamilySubDataLimitRow row = mock(FamilySubDataLimitRow.class);
        given(row.getFamilyId()).willReturn(1L);
        given(row.getName()).willReturn("김태연");
        given(row.getIsLocked()).willReturn(false);
        given(row.getDataLimit()).willReturn(5242880L);
        given(row.getFamilyDataAmount()).willReturn(25165824L);

        given(familySubscriptionJpaRepository.findDataLimitBySubId(subId)).willReturn(Optional.of(row));

        // when
        FamilySubDataLimit result = familySubscriptionRepository.findDataLimitBySubId(subId);

        // then
        assertThat(result.getFamilyId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("김태연");
        assertThat(result.getIsLocked()).isFalse();
        assertThat(result.getDataLimit()).isEqualTo(5242880L);
        assertThat(result.getFamilyDataAmount()).isEqualTo(25165824L);
    }

    @Test
    @DisplayName("회선 ID로 데이터 한도 조회 시 정보가 없으면 예외 발생")
    void findDataLimitBySubIdNotFound() {
        // given
        Long subId = 999L;
        given(familySubscriptionJpaRepository.findDataLimitBySubId(subId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> familySubscriptionRepository.findDataLimitBySubId(subId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
    }
}
