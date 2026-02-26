package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindDataLimitService;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.controller.response.FindDataLimitResponse;
import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilySubDataLimitMapper;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindDataLimitServiceImpl implements FindDataLimitService {
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FindFamilySubscriptionService familySubscriptionService;

    @Override
    public FindDataLimitResponse findDataLimit(
            Long targetSubId,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // OWNER인지 확인
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);
        }

        // 대상자 가족 정보 조회
        FamilySubscription targetFamilySub = familySubscriptionService.findBySubId(targetSubId);

        // 같은 가족 구성원인지 확인
        if (!requesterFamilyId.equals(targetFamilySub.getFamily().getId())) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 최적화된 쿼리로 한도 정보 조회
        FamilySubDataLimit familySubDataLimit = familySubscriptionRepository.findDataLimitBySubId(targetSubId);

        return FamilySubDataLimitMapper.toFindDataLimitResponse(familySubDataLimit);
    }
}
