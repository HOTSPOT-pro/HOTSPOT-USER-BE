package hotspot.user.subscription.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.member.domain.Member;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

/**
 * 회선 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionRepositoryImplTest {

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    @InjectMocks
    private SubscriptionRepositoryImpl subscriptionRepository;

    @Test
    @DisplayName("전화번호 해시로 회선 조회 성공")
    void findByPhoneHashSuccess() {
        String hash = "hashed-phone";

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .subId(100L)
                .phoneHash(hash)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();

        given(subscriptionJpaRepository.findByPhoneHash(hash))
                .willReturn(Optional.of(entity));

        Optional<Subscription> result =
                subscriptionRepository.findByPhoneHash(hash);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
        assertThat(result.get().getPhoneHash()).isEqualTo(hash);
    }

    @Test
    @DisplayName("회선 ID로 회선 조회 성공")
    void findByIdSuccess() {

        Long id = 100L;

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .subId(id)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();

        given(subscriptionJpaRepository.findById(id))
                .willReturn(Optional.of(entity));

        Optional<Subscription> result =
                subscriptionRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("멤버 ID로 회선 조회 성공")
    void findByMemberIdSuccess() {

        Long memberId = 1L;

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .subId(100L)
                .member(MemberEntity.builder().id(memberId).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();

        given(subscriptionJpaRepository.findByMemberId(memberId))
                .willReturn(Optional.of(entity));

        Optional<Subscription> result =
                subscriptionRepository.findByMemberId(memberId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("회선 저장 성공")
    void saveSubscriptionSuccess() {

        Subscription subscription = Subscription.builder()
                .id(100L)
                .member(Member.builder().id(1L).build())
                .plan(Plan.builder().id(1L).build())
                .phoneEnc("enc")
                .phoneHash("hash")
                .isLocked(false)
                .build();

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .subId(100L)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();

        given(subscriptionJpaRepository.save(any(SubscriptionEntity.class)))
                .willReturn(entity);

        Subscription result =
                subscriptionRepository.save(subscription);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("subIds로 DataPeriod 조회 성공")
    void findDataPeriodsBySubIdsSuccess() {

        // given
        SubscriptionEntity sub1 = SubscriptionEntity.builder()
                .subId(1L)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder()
                        .planId(1L)
                        .dataPeriod(DataPeriod.MONTH)
                        .build())
                .build();

        SubscriptionEntity sub2 = SubscriptionEntity.builder()
                .subId(2L)
                .member(MemberEntity.builder().id(2L).build())
                .plan(PlanEntity.builder()
                        .planId(2L)
                        .dataPeriod(DataPeriod.DAY)
                        .build())
                .build();

        given(subscriptionJpaRepository.findAllByIdIn(List.of(1L, 2L)))
                .willReturn(List.of(sub1, sub2));

        // when
        Map<Long, DataPeriod> result =
                subscriptionRepository.findDataPeriodsBySubIds(List.of(1L, 2L));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1L)).isEqualTo(DataPeriod.MONTH);
        assertThat(result.get(2L)).isEqualTo(DataPeriod.DAY);
    }

    @Test
    @DisplayName("전화번호 해시 리스트로 여러 회선 정보 일괄 조회 성공")
    void findAllByPhoneHashInSuccess() {
        // given
        List<String> hashes = List.of("hash1", "hash2");
        SubscriptionEntity entity1 = SubscriptionEntity.builder()
                .subId(100L).phoneHash("hash1").build();
        SubscriptionEntity entity2 = SubscriptionEntity.builder()
                .subId(101L).phoneHash("hash2").build();

        given(subscriptionJpaRepository.findAllByPhoneHashIn(hashes))
                .willReturn(List.of(entity1, entity2));

        // when
        List<Subscription> result = subscriptionRepository.findAllByPhoneHashIn(hashes);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPhoneHash()).isEqualTo("hash1");
        assertThat(result.get(1).getPhoneHash()).isEqualTo("hash2");
    }

    @Test
    @DisplayName("subIds가 비어있으면 빈 Map 반환")
    void findDataPeriodsBySubIdsEmpty() {

        Map<Long, DataPeriod> result =
                subscriptionRepository.findDataPeriodsBySubIds(List.of());

        assertThat(result).isEmpty();
    }
}
