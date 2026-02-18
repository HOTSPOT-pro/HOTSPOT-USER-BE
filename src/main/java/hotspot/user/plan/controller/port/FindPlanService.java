package hotspot.user.plan.controller.port;


import hotspot.user.plan.controller.response.PlanResponse;

/**
 * 요금제 조회 서비스
 */
public interface FindPlanService {
    PlanResponse findById(Long id);
}
