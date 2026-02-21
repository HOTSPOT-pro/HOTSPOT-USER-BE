package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.UpdateDataLimitService;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilySubscriptionMapper;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateDataLimitServiceImpl implements UpdateDataLimitService {
    private final FamilySubscriptionRepository familySubscriptionRepository;

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

        // 3. 데이터 한도 업데이트
        familySub.updateDataLimit(request.dataLimit());

        FamilySubscription savedFamilySub = familySubscriptionRepository.save(familySub);

        return FamilySubscriptionMapper.toUpdateDataLimitResponse(savedFamilySub);
    }
}
