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
}
