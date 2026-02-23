package hotspot.user.family.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.family.controller.port.CreateFamilyApplyService;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilyApplyMapper;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가족 구성원 추가 / 삭제 신청 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional
public class CreateFamilyApplyServiceImpl implements CreateFamilyApplyService {
    private final FamilyApplyRepository familyApplyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public CreateFamilyApplyResponse manage(
            Long requesterMemberId,
            Long familyId,
            FamilyRole requesterFamilyRole,
            CreateFamilyApplyRequest request) {

        // 1. 신청자의 역할이 OWNER인지 확인
        if (requesterFamilyRole != FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);
        }

        // 2. 가입된 회선 있는지 확인
        Subscription requesterSub = subscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 3. 피신청자 회선 유효성 확인
        Subscription targetSub = subscriptionRepository.findById(request.targetSubId())
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 4. 타입별 비즈니스 규칙 검증
        validateApplyType(familyId, request, targetSub.getId());

        FamilyApply familyApply = FamilyApplyMapper.toFamilyApply(
                requesterSub.getId(),
                familyId,
                requesterFamilyRole,
                request
        );

        FamilyApply savedFamilyApply = familyApplyRepository.save(familyApply);

        return FamilyApplyMapper.toCreateFamilyApplyResponse(savedFamilyApply);
    }

    private void validateApplyType(Long familyId, CreateFamilyApplyRequest request, Long targetSubId) {
        FamilySubscription targetFamilySub = familySubscriptionRepository.findBySubId(targetSubId).orElse(null);

        if (request.applyType() == ApplyType.ADD) {
            // 이미 가족에 소속되어 있는지 확인
            if (targetFamilySub != null) {
                throw new ApplicationException(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY);
            }
            // 서류 URL 및 역할 필수 체크
            if (request.docUrl() == null || request.docUrl().isBlank()) {
                throw new ApplicationException(FamilyErrorCode.DOC_URL_REQUIRED);
            }
            if (request.targetFamilyRole() == null) {
                throw new ApplicationException(FamilyErrorCode.TARGET_ROLE_REQUIRED);
            }
        } else if (request.applyType() == ApplyType.REMOVE) {
            // 삭제 시에는 같은 가족 구성원인지 확인
            if (targetFamilySub == null || !targetFamilySub.getFamily().getId().equals(familyId)) {
                throw new ApplicationException(FamilyErrorCode.TARGET_NOT_IN_FAMILY);
            }
        }
    }
}
