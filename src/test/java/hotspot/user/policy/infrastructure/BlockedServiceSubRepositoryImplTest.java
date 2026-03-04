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
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;
import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class BlockedServiceSubRepositoryImplTest {

    @Mock
    private BlockedServiceSubJpaRepository jpaRepository;

    @InjectMocks
    private BlockedServiceSubRepositoryImpl repository;

    @Test
    @DisplayName("회선 ID로 모든 차단된 앱 목록 조회 성공(비활성 포함)")
    void findBySubIdSuccess() {
        // given
        Long subId = 100L;
        SubscriptionEntity subEntity = SubscriptionEntity.builder()
                .subId(subId)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();
        AppBlockedServiceEntity appEntity = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(1L)
                .blockedServiceName("YouTube")
                .blockedServiceCode("YOUTUBE")
                .build();

        BlockedServiceSubEntity entity = BlockedServiceSubEntity.builder()
                .blockedServiceSubId(10L)
                .subscription(subEntity)
                .appBlockedService(appEntity)
                .isActive(true)
                .build();

        given(jpaRepository.findBySubscriptionSubId(subId)).willReturn(List.of(entity));

        // when
        List<BlockedServiceSub> result = repository.findBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppBlockedServiceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회선 ID로 활성화된 앱 목록만 조회 성공")
    void findActiveBySubIdSuccess() {
        // given
        Long subId = 100L;
        SubscriptionEntity subEntity = SubscriptionEntity.builder()
                .subId(subId)
                .member(MemberEntity.builder().id(1L).build())
                .plan(PlanEntity.builder().planId(1L).build())
                .build();
        AppBlockedServiceEntity appEntity = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(1L)
                .blockedServiceName("YouTube")
                .blockedServiceCode("YOUTUBE")
                .build();

        BlockedServiceSubEntity entity = BlockedServiceSubEntity.builder()
                .blockedServiceSubId(10L)
                .subscription(subEntity)
                .appBlockedService(appEntity)
                .isActive(true)
                .build();

        given(jpaRepository.findBySubscriptionSubIdAndIsActiveTrue(subId)).willReturn(List.of(entity));

        // when
        List<BlockedServiceSub> result = repository.findActiveBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("회선 ID로 차단된 서비스 ID 리스트만 조회 성공")
    void findActiveServiceIdsBySubIdSuccess() {
        // given
        Long subId = 100L;
        given(jpaRepository.findActiveServiceIdsBySubId(subId)).willReturn(List.of(1L, 2L));

        // when
        List<Long> result = repository.findActiveServiceIdsBySubId(subId);

        // then
        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("성공: 도메인 리스트를 받아 신규 저장, 활성화, 비활성화를 일괄 처리한다")
    void saveAllSuccess() {
        // given
        List<BlockedServiceSub> domains = List.of(
                BlockedServiceSub.builder().subId(100L).appBlockedServiceId(1L).isActive(true).build(), // 신규
                BlockedServiceSub.builder().id(10L).subId(100L).appBlockedServiceId(2L).isActive(true).build(), // 활성화
                BlockedServiceSub.builder().id(11L).subId(100L).appBlockedServiceId(3L).isActive(false).build() // 비활성화
        );

        // when
        repository.saveAll(domains);

        // then
        verify(jpaRepository, times(1)).saveAll(anyList()); // 1건 신규 저장
        verify(jpaRepository, times(1)).bulkActivate(anyList()); // 10L 활성화
        verify(jpaRepository, times(1)).bulkDeactive(anyList()); // 11L 비활성화
    }
}
