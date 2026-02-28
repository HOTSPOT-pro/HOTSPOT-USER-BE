package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;
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
    @DisplayName("회선 ID로 적용된 정책 목록 조회 성공")
    void findBySubIdSuccess() {
        // given
        Long subId = 100L;
        SubscriptionEntity subEntity = SubscriptionEntity.builder()
                .subId(subId)
                .build();
        BlockPolicyEntity policyEntity = BlockPolicyEntity.builder()
                .blockPolicyId(1L)
                .build();

        PolicySubEntity entity = PolicySubEntity.builder()
                .policySubId(10L)
                .subscription(subEntity)
                .blockPolicy(policyEntity)
                .isActive(true)
                .build();

        given(jpaRepository.findBySubscriptionSubId(subId)).willReturn(List.of(entity));

        // when
        List<PolicySub> result = repository.findBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBlockPolicyId()).isEqualTo(1L);
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("성공: 신규 PolicySub 정보를 저장한다")
    void saveAllSuccessWithNewEntities() {
        // given
        PolicySub domain = PolicySub.builder().id(null).subId(1L).blockPolicyId(1L).isActive(true).build();
        PolicySubEntity entity = PolicySubEntity.builder().policySubId(1L).build();
        given(jpaRepository.saveAll(anyList())).willReturn(List.of(entity));

        // when
        List<PolicySub> result = repository.saveAll(List.of(domain));

        // then
        assertThat(result).hasSize(1);
        verify(jpaRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("성공: 기존 PolicySub를 비활성화(Soft Delete)한다")
    void saveAllSuccessWithDeactivate() {
        // given
        PolicySub domain = PolicySub.builder().id(10L).subId(1L).blockPolicyId(1L).isActive(false).build();

        // when
        List<PolicySub> result = repository.saveAll(List.of(domain));

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).bulkSoftDelete(anyList());
    }

    @Test
    @DisplayName("성공: 기존 비활성화된 PolicySub를 다시 활성화한다")
    void saveAllSuccessWithActivate() {
        // given
        PolicySub domain = PolicySub.builder().id(10L).subId(1L).blockPolicyId(1L).isActive(true).build();

        // when
        List<PolicySub> result = repository.saveAll(List.of(domain));

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).bulkActivate(anyList());
    }
}
