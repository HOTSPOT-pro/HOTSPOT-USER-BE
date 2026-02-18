package hotspot.user.plan.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.plan.infrastructure.entity.PlanEntity;

/**
 * 요금제 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class PlanRepositoryImplTest {

    @Mock
    private PlanJpaRepository planJpaRepository;

    @InjectMocks
    private PlanRepositoryImpl planRepository;

    @Test
    @DisplayName("요금제 ID로 요금제 정보 조회 성공")
    void findByIdSuccess() {
        // given
        Long planId = 1L;
        PlanEntity entity = PlanEntity.builder()
                .planId(planId)
                .planName("LTE 기본 요금제")
                .planDataAmount(10)
                .dataPeriod(DataPeriod.MONTH)
                .build();

        given(planJpaRepository.findByPlanId(planId)).willReturn(Optional.of(entity));

        // when
        Optional<Plan> result = planRepository.findById(planId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(planId);
        assertThat(result.get().getName()).isEqualTo("LTE 기본 요금제");
        assertThat(result.get().getDataAmount()).isEqualTo(10);
        assertThat(result.get().getDataPeriod()).isEqualTo(DataPeriod.MONTH);
    }
}
