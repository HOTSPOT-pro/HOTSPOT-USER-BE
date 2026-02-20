package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.policy.domain.DateSnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.infrastructure.entity.PolicySubEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

/**
 * 정책-회선 조회 매핑 Repository 단위 테스트
 */

@ExtendWith(MockitoExtension.class)
class PolicySubRepositoryImplTest {

    @Mock
    private PolicySubJpaRepository jpaRepository;

    @InjectMocks
    private PolicySubRepositoryImpl repository;

    @Test
    @DisplayName("회선 ID로 적용된 시간 정책 목록 조회 성공")
    void findBySubIdSuccess() {
        // given
        Long subId = 100L;
        SubscriptionEntity subEntity = SubscriptionEntity.builder()
                .subId(subId)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();
        DateSnapshot snapshot = DateSnapshot.builder()
                .policyName("수면 모드")
                .build();

        PolicySubEntity entity = PolicySubEntity.builder()
                .policySubId(10L)
                .subscription(subEntity)
                .dateSnapshot(snapshot)
                .build();

        given(jpaRepository.findBySubscriptionSubId(subId)).willReturn(List.of(entity));

        // when
        List<PolicySub> result = repository.findBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDateSnapshot().getPolicyName()).isEqualTo("수면 모드");
    }
}
