package hotspot.user.plan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PlanErrorCode;
import hotspot.user.plan.controller.response.PlanResponse;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.plan.service.port.PlanRepository;

/**
 * 요금제 조회 서비스에 대한 테스트 코드 - FindPlanServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class FindPlanServiceImplTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private FindPlanServiceImpl findPlanService;

    @Test
    @DisplayName("요금제 ID로 조회 시 존재하는 경우 PlanResponse를 반환한다")
    void findByIdSuccess() {
        // given
        Long planId = 1L;
        Plan plan = Plan.builder()
                .id(planId)
                .name("베이직 요금제")
                .dataAmount(100L)
                .dataPeriod(DataPeriod.MONTH)
                .build();

        given(planRepository.findById(planId)).willReturn(Optional.of(plan));

        // when
        PlanResponse result = findPlanService.findById(planId);

        // then
        assertThat(result.name()).isEqualTo("베이직 요금제");
        assertThat(result.dataAmount()).isEqualTo(100L);
        assertThat(result.dataPeriod()).isEqualTo(DataPeriod.MONTH);
    }

    @Test
    @DisplayName("요금제 ID로 조회 시 존재하지 않는 경우 예외가 발생한다")
    void findByIdFail() {
        // given
        Long planId = 999L;
        given(planRepository.findById(planId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findPlanService.findById(planId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(PlanErrorCode.PLAN_NOT_FOUND.getMessage());
    }
}
