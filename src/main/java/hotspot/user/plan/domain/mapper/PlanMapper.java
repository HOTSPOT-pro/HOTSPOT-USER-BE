package hotspot.user.plan.domain.mapper;

import hotspot.user.plan.controller.response.PlanResponse;
import hotspot.user.plan.domain.Plan;

/**
 * request Dto -> 도메인
 * 도메인 -> response Dto
 */
public class PlanMapper {
    // request -> domain

    // domain -> response
    public static PlanResponse toPlanResponse(Plan plan) {
        return new PlanResponse(plan.getName(), plan.getDataAmount(), plan.getDataPeriod());
    }
}
