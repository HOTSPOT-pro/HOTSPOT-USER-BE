package hotspot.user.family.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
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
import hotspot.user.outbox.consistencyOutbox.domain.event.family.limit.FamilySubLimitChangedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateDataLimitServiceImpl implements UpdateDataLimitService {

    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final long GB_TO_KB_UNIT = 1_048_576L;

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

        long newLimitKb = request.dataLimit() * GB_TO_KB_UNIT;

        // 2️⃣ Outbox 이벤트 발행 (스냅샷)
        publishFamilyLimitChangedEvent(
                savedFamilySub.getFamily().getId(),
                savedFamilySub.getSubscription().getId(),
                newLimitKb
        );

        return FamilySubscriptionMapper.toUpdateDataLimitResponse(savedFamilySub);
    }

    private void publishFamilyLimitChangedEvent(
            Long familyId,
            Long subId,
            Long newLimit
    ) {
        eventPublisher.publishEvent(
                new FamilySubLimitChangedEvent(
                        "FAMILY_SUB_LIMIT_CHANGED",
                        familyId,
                        subId,
                        newLimit,
                        UUID.randomUUID().toString()
                )
        );
    }
}
