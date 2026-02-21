package hotspot.user.policy.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
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
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public UpdateAppBlockedServiceResponse updateAppBlockedService(
            UpdateAppBlockedServiceRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // 1. OWNER 권한 체크
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        Long subId = request.subId();

        // 2. 수정 대상 회선이 존재하는지 & 요청자와 같은 가족인지 확인
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 3. 기존 DB 상태 조회 (현재 차단된 ID들만 직접 조회하여 NPE 방지)
        Set<Long> existingIds = new HashSet<>(blockedServiceSubRepository.findActiveServiceIdsBySubId(subId));

        // 4. 차단되어야할 앱 서비스 ID (목표 상태)
        Set<Long> targetIds = new HashSet<>(request.blockedServiceIdList());

        // 5. 차집합 계산
        // (1) 새로 추가해야 할 ID들 (목표 리스트 - 기존 리스트)
        Set<Long> toAddIds = new HashSet<>(targetIds);
        toAddIds.removeAll(existingIds);

        // (2) 차단 해제해야 할 ID들 (기존 리스트 - 목표 리스트)
        Set<Long> toRemoveIds = new HashSet<>(existingIds);
        toRemoveIds.removeAll(targetIds);

        // 6. 벌크 연산 수행 (쿼리 최소화)
        if (!toAddIds.isEmpty()) {
            blockedServiceSubRepository.saveAll(subId, toAddIds);
        }
        if (!toRemoveIds.isEmpty()) {
            blockedServiceSubRepository.deleteAll(subId, toRemoveIds);
        }

        // 7. 최종 동기화 결과 재조회 및 반환 (ID 리스트만 직접 조회)
        List<Long> finalBlockedIdList = blockedServiceSubRepository.findActiveServiceIdsBySubId(subId);

        return AppBlockedServiceMapper.toUpdateAppBlockedServiceResponse(
                request.familyId(),
                subId,
                finalBlockedIdList);
    }
}
