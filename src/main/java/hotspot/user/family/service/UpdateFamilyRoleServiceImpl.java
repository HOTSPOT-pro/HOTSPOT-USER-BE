package hotspot.user.family.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.UpdateFamilyRoleService;
import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilySubscriptionMapper;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFamilyRoleServiceImpl implements UpdateFamilyRoleService {

    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public UpdateFamilyRoleResponse update(
            Long requesterFamilyId, FamilyRole requesterFamilyRole,
            Long targetFamilyId, Long targetSubId,
            UpdateFamilyRoleRequest request) {

        // 1. OWNER 권한 체크
        if (requesterFamilyRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        FamilySubscription familySub = familySubscriptionRepository.findBySubId(targetSubId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 2. 같은 가족 구성원인지 체크
        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 3. 가족 내 역할 업데이트
        familySub.updateFamilyRole(request.familyRole());

        FamilySubscription savedFamilySub = familySubscriptionRepository.save(familySub);

        return FamilySubscriptionMapper.toUpdateFamilyRoleResponse(savedFamilySub);
    }
}
