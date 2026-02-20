package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.UpdateDataLimitService;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilySubscriptionMapper;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateDataLimitServiceImpl implements UpdateDataLimitService {
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public UpdateDataLimitResponse updateDataLimit(UpdateDataLimitRequest request) {
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(request.subId())
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        familySub.updateDataLimit(request.dataLimit());

        familySubscriptionRepository.save(familySub);

        return FamilySubscriptionMapper.toUpdateDataLimitResponse(familySub);
    }
}
