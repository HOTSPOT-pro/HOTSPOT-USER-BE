package hotspot.user.family.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.family.controller.port.UpdateFamilyRoleService;
import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilySubscriptionMapper;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFamilyRoleServiceImpl implements UpdateFamilyRoleService {

    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public UpdateFamilyRoleResponse update(
            Long requesterMemberId, Long requesterFamilyId, FamilyRole requesterFamilyRole,
            Long targetSubId,
            UpdateFamilyRoleRequest request) {

        // 1. OWNER 권한 체크 (가족 관리자만 역할 변경 가능)
        if (requesterFamilyRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        // 2. 본인 역할 변경 시도 차단 (가족 내 OWNER 부재 방지)
        Subscription requesterSub = subscriptionRepository.findById(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (requesterSub.getId().equals(targetSubId)) {
            throw new ApplicationException(FamilyErrorCode.CANNOT_CHANGE_OWNER_ROLE);
        }

        // 3. 대상 조회
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(targetSubId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 4. 같은 가족 구성원인지 체크
        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 5. 타인을 OWNER로 임명하는 시도 차단
        if (request.familyRole() == FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE);
        }

        // 6. 가족 내 역할 업데이트
        familySub.updateFamilyRole(request.familyRole());
        familySubscriptionRepository.save(familySub);

        return FamilySubscriptionMapper.toUpdateFamilyRoleResponse(familySub);
    }
}
