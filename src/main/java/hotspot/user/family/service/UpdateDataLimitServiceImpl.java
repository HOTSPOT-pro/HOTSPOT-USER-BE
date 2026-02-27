package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.constant.FamilyConstant;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.UpdateDataLimitService;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilySubscriptionMapper;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 구성원 데이터 한도 및 즉시 차단 여부 업데이트 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateDataLimitServiceImpl implements UpdateDataLimitService {
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public UpdateDataLimitResponse updateDataLimit(UpdateDataLimitRequest request,
                                                   Long requesterFamilyId,
                                                   FamilyRole requesterRole) {
        // 1. OWNER 권한 체크
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        FamilySubscription familySub = familySubscriptionRepository.findBySubId(request.subId())
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 2. 같은 가족 구성원인지 체크
        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 3. 데이터 한도 유효성 검증
        if (request.dataLimit() < FamilyConstant.UNLIMITED_DATA_LIMIT) {
            throw new ApplicationException(FamilyErrorCode.INVALID_DATA_LIMIT);
        }

        // 4. 데이터 한도 업데이트
        // GB -> KB 변환 (-1은 무제한이므로 그대로 유지)
        long dataLimitKb = (request.dataLimit() == FamilyConstant.UNLIMITED_DATA_LIMIT)
                ? FamilyConstant.UNLIMITED_DATA_LIMIT
                : request.dataLimit() * 1024L * 1024L;

        // 가족 전체 데이터 양 초과 여부 검증 (무제한 설정은 제외)
        if (dataLimitKb != FamilyConstant.UNLIMITED_DATA_LIMIT &&
            dataLimitKb > familySub.getFamily().getFamilyDataAmount()) {
            throw new ApplicationException(FamilyErrorCode.DATA_LIMIT_EXCEEDS_FAMILY_AMOUNT);
        }

        familySubscriptionRepository.updateDataLimit(request.subId(), dataLimitKb);

        // 5. 차단 여부 업데이트
        subscriptionRepository.updateLockedStatus(request.subId(), request.isLocked());

        // 6. 실제 DB에서 최종 상태를 다시 읽어와서 응답 (데이터 정합성 보장)
        FamilySubDataLimit savedDataLimit = familySubscriptionRepository.findDataLimitBySubId(request.subId());
        return FamilySubscriptionMapper.toUpdateDataLimitResponse(request.subId(), savedDataLimit);
    }
}
