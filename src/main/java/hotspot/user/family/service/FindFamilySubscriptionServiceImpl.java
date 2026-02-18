package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.controller.response.FamilySubscriptionResponse;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성원(회선) 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilySubscriptionServiceImpl implements FindFamilySubscriptionService {

    private final FamilySubscriptionRepository repository;

    @Override
    public FamilySubscriptionResponse findBySubId(Long subId) {
        return repository.findBySubId(subId)
                .map(FamilySubscriptionResponse::from)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));
    }
}
