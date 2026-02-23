package hotspot.user.family.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.family.controller.port.CreateFamilyApplyService;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.mapper.FamilyApplyMapper;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 가족 구성원 추가 / 삭제 신청 서비스 구현체
 */

@Service
@RequiredArgsConstructor
public class CreateFamilyServiceImpl implements CreateFamilyApplyService {
    private final FamilyApplyRepository familyApplyRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public CreateFamilyApplyResponse manage(
            Long requesterMemberId,
            Long familyId,
            FamilyRole requesterFamilyRole,
            CreateFamilyApplyRequest request) {

        // 신청자의 역할이 OWNER인지 확인
        // 같은 가족 구성원인지 확인

        // 가입된 회선 있는지 예외 처리 필요
        Subscription subscription = subscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));


        FamilyApply familyApply = FamilyApplyMapper.toFamilyApply(
                subscription.getId(),
                familyId,
                requesterFamilyRole,
                request
        );

        FamilyApply savedFamilyApply = familyApplyRepository.save(familyApply);

        return FamilyApplyMapper.toCreateFamilyApplyResponse(savedFamilyApply);

    }
}
