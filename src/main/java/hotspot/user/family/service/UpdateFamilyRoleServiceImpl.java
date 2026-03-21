package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFamilyRoleServiceImpl implements UpdateFamilyRoleService {

    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public UpdateFamilyRoleResponse update(
            Long requesterMemberId, Long requesterFamilyId, FamilyRole requesterFamilyRole,
            Long targetSubId,
            UpdateFamilyRoleRequest request) {

        // 1. OWNER 권한 체크
        if (requesterFamilyRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        // 2. 대상 조회
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(targetSubId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 3. 본인 역할 변경 시도 차단 (가족 내 OWNER 부재 방지)
        if (familySub.getSubscription().getMember().getId().equals(requesterMemberId)) {
            throw new ApplicationException(FamilyErrorCode.CANNOT_CHANGE_OWNER_ROLE);
        }

        // 4. 같은 가족 구성원인지 체크
        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 5. 타인을 OWNER로 업데이트 하는 시도 차단
        if (request.familyRole() == FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE);
        }

        // 6. 변경하려는 역할이 현재와 동일한 경우 처리 생략
        if (familySub.getFamilyRole() == request.familyRole()) {
            return FamilySubscriptionMapper.toUpdateFamilyRoleResponse(familySub);
        }

        // 7. 가족 내 역할 업데이트
        familySub.updateFamilyRole(request.familyRole());
        familySubscriptionRepository.save(familySub);

        return FamilySubscriptionMapper.toUpdateFamilyRoleResponse(familySub);
    }
}
