package hotspot.user.policy.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.controller.port.UpdateAppBlockedServiceService;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import hotspot.user.policy.domain.mapper.AppBlockedServiceMapper;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import lombok.RequiredArgsConstructor;

/**
 * 구성원별 차단 앱 서비스 업데이트 서비스 코드 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAppBlockedServiceServiceImpl implements UpdateAppBlockedServiceService {

    private final BlockedServiceSubRepository blockedServiceSubRepository;

    @Override
    public UpdateAppBlockedServiceResponse updateAppBlockedService(UpdateAppBlockedServiceRequest request) {
        Long subId = request.subId();

        // 기존 DB 상태 조회 (현재 차단된 ID들)
        Set<Long> existingIds = blockedServiceSubRepository.findBySubId(subId).stream()
                .map(blocked -> blocked.getAppBlockedService().getId())
                .collect(Collectors.toSet());

        // 차단되어야할 앱 서비스 ID
        Set<Long> targetIds = new HashSet<>(request.blockedServiceIdList());

        // 차집합 계산
        // (1) 새로 추가해야 할 ID들 (목표 리스트 - 기존 리스트)
        Set<Long> toAddIds = new HashSet<>(targetIds);
        toAddIds.removeAll(existingIds);

        // (2) 차단 해제해야 할 ID들 (기존 리스트 - 목표 리스트)
        Set<Long> toRemoveIds = new HashSet<>(existingIds);
        toRemoveIds.removeAll(targetIds);

        // 벌크 연산 수행 (쿼리 최소화)
        if (!toAddIds.isEmpty()) {
            blockedServiceSubRepository.saveAll(subId, toAddIds);
        }
        if (!toRemoveIds.isEmpty()) {
            blockedServiceSubRepository.deleteAll(subId, toRemoveIds);
        }

        // 5단계: 최종 동기화 결과 재조회 및 반환
        List<Long> finalBlockedIdList = blockedServiceSubRepository.findBySubId(subId).stream()
                .map(blocked -> blocked.getAppBlockedService().getId())
                .toList();

        return AppBlockedServiceMapper.toUpdateAppBlockedServiceResponse(
                request.familyId(),
                subId,
                finalBlockedIdList);
    }
}
