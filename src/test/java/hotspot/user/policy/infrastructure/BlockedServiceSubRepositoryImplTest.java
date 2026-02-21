package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

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
    @DisplayName("회선 ID로 차단된 앱 목록 조회 성공")
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
                .build();

        given(jpaRepository.findBySubscriptionSubId(subId)).willReturn(List.of(entity));

        // when
        List<BlockedServiceSub> result = repository.findBySubId(subId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppBlockedService().getName()).isEqualTo("YouTube");
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
    @DisplayName("성공: 새로운 서비스를 차단하거나 기존 이력을 복구한다 (saveAll)")
    void saveAllSuccess() {
        // given
        Long subId = 100L;
        Set<Long> serviceIds = Set.of(1L);

        BlockedServiceSubEntity existing = BlockedServiceSubEntity.builder()
                .blockedServiceSubId(10L)
                .isDeleted(true)
                .appBlockedService(AppBlockedServiceEntity.builder().appBlockedServiceId(1L).build())
                .subscription(SubscriptionEntity.builder().subId(subId).build())
                .build();

        given(jpaRepository.findBySubIdAndServiceIdsIncludeDeleted(eq(subId), anySet()))
                .willReturn(List.of(existing));

        // when
        repository.saveAll(subId, serviceIds);

        // then
        verify(jpaRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("성공: 요청받은 서비스들을 일괄 차단 해제한다 (deleteAll)")
    void deleteAllSuccess() {
        // given
        Long subId = 100L;
        Set<Long> serviceIds = Set.of(1L, 2L);

        // when
        repository.deleteAll(subId, serviceIds);

        // then
        verify(jpaRepository, times(1)).bulkSoftDelete(anyLong(), anySet());
    }
}
