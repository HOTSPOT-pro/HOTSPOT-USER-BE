package hotspot.user.plan.controller.response;

import hotspot.user.plan.domain.DataPeriod;

public record PlanResponse(
        String name,
        long dataAmount,
        DataPeriod dataPeriod
) {
}
