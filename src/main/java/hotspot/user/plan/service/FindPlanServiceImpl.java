package hotspot.user.plan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PlanErrorCode;
import hotspot.user.plan.controller.port.FindPlanService;
import hotspot.user.plan.controller.response.PlanResponse;
import hotspot.user.plan.domain.mapper.PlanMapper;
import hotspot.user.plan.service.port.PlanRepository;
import lombok.RequiredArgsConstructor;

/**
 * 요금제 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindPlanServiceImpl implements FindPlanService {

    private final PlanRepository planRepository;

    @Override
    public PlanResponse findById(Long id) {
        return planRepository.findById(id)
                .map(PlanMapper::toPlanResponse)
                .orElseThrow(() -> new ApplicationException(PlanErrorCode.PLAN_NOT_FOUND));
    }
}
