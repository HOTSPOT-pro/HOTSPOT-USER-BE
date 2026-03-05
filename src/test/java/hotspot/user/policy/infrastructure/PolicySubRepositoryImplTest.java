package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;
import hotspot.user.policy.infrastructure.entity.PolicySubEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 정책-회선 매핑 Repository 구현체(PolicySubRepositoryImpl) 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class PolicySubRepositoryImplTest {

    @Mock
    private PolicySubJpaRepository jpaRepository;

    @InjectMocks
    private PolicySubRepositoryImpl repository;

    @Test
    @DisplayName("성공: 회선 ID로 해당 회선의 모든 정책 매핑 정보를 조회한다 (활성+비활성)")
    void findBySubIdSuccess() {
        // given
        Long subId = 1L;
        LocalDateTime now = LocalDateTime.now();
        PolicySubEntity entity = createEntity(10L, subId, 50L, true, now);
        given(jpaRepository.findBySubscriptionSubId(subId)).willReturn(List.of(entity));

        // when
        List<PolicySub> result = repository.findBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getSubId()).isEqualTo(subId);
        assertThat(result.get(0).getBlockPolicyId()).isEqualTo(50L);
        assertThat(result.get(0).getModifiedTime()).isEqualTo(now);
        verify(jpaRepository).findBySubscriptionSubId(subId);
    }

    @Test
    @DisplayName("성공: 회선 ID로 활성화된 정책 매핑 정보만 조회한다")
    void findActiveBySubIdSuccess() {
        // given
        Long subId = 1L;
        LocalDateTime now = LocalDateTime.now();
        PolicySubEntity activeEntity = createEntity(10L, subId, 50L, true, now);
        given(jpaRepository.findBySubscriptionSubIdAndIsActiveTrue(subId)).willReturn(List.of(activeEntity));

        // when
        List<PolicySub> result = repository.findActiveBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(0).getModifiedTime()).isEqualTo(now);
        verify(jpaRepository).findBySubscriptionSubIdAndIsActiveTrue(subId);
    }

    @Test
    @DisplayName("성공: 복합 상태의 도메인 리스트 저장 시 신규/활성/비활성을 분류하여 각각 처리한다")
    void saveAllComplexSuccess() {
        // given
        // 1. 신규 저장 대상 (ID 없음)
        PolicySub newDomain = PolicySub.builder().subId(1L).blockPolicyId(100L).isActive(true).build();
        // 2. 비활성화 대상 (ID 있음, isActive false)
        PolicySub deactivateDomain = PolicySub.builder().id(20L).isActive(false).build();
        // 3. 활성화 대상 (ID 있음, isActive true)
        PolicySub activateDomain = PolicySub.builder().id(30L).isActive(true).build();

        List<PolicySub> domainList = List.of(newDomain, deactivateDomain, activateDomain);

        PolicySubEntity savedEntity = createEntity(1L, 1L, 100L, true, LocalDateTime.now());
        given(jpaRepository.saveAll(anyList())).willReturn(List.of(savedEntity));

        // when
        List<PolicySub> result = repository.saveAll(domainList);

        // then
        // 1. 신규 저장은 saveAll 호출
        verify(jpaRepository, times(1)).saveAll(anyList());
        // 2. 비활성화는 bulkDeActive 호출
        verify(jpaRepository, times(1)).bulkDeActive(anyList());
        // 3. 활성화는 bulkActivate 호출
        verify(jpaRepository, times(1)).bulkActivate(anyList());

        assertThat(result).hasSize(1); // saveAll의 리턴값 기준
    }

    @Test
    @DisplayName("성공: 정책 ID 리스트로 해당 정책을 적용 중인 모든 회선 매핑 정보를 벌크 비활성화한다")
    void bulkDeActiveByBlockPolicyIdsSuccess() {
        // given
        List<Long> blockPolicyIds = List.of(50L, 60L);

        // when
        repository.bulkDeActiveByBlockPolicyIds(blockPolicyIds);

        // then
        verify(jpaRepository).bulkDeActiveByBlockPolicyIds(blockPolicyIds);
    }

    /**
     * 테스트용 PolicySubEntity 생성 유틸리티
     */
    private PolicySubEntity createEntity(Long id, Long subId, Long policyId, boolean isActive, LocalDateTime modifiedTime) {
        SubscriptionEntity subEntity = SubscriptionEntity.builder().subId(subId).build();
        BlockPolicyEntity policyEntity = BlockPolicyEntity.builder().blockPolicyId(policyId).build();

        PolicySubEntity entity = PolicySubEntity.builder()
                .policySubId(id)
                .subscription(subEntity)
                .subId(subId) // 조회용 필드
                .blockPolicy(policyEntity)
                .blockPolicyId(policyId) // 조회용 필드
                .isActive(isActive)
                .build();

        if (modifiedTime != null) {
            ReflectionTestUtils.setField(entity, "modifiedTime", modifiedTime);
        }
        return entity;
    }
}
