package hotspot.user.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.AppliedPolicyMapper;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMemberAppliedPolicyServiceImpl implements FindMemberAppliedPolicyService {

    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final PolicySubRepository policySubRepository;
    private final BlockedServiceSubRepository blockedServiceSubRepository;

    @Override
    public AppliedPolicyResponse findByMemberId(Long memberId) {
        FamilySubscription familySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        Long subId = familySub.getSubscription().getId();

        List<PolicySub> policySubs = policySubRepository.findBySubId(subId);
        List<BlockedServiceSub> blockedServiceSubs = blockedServiceSubRepository.findBySubId(subId);

        // 매퍼의 통합 조립 메서드 호출
        return AppliedPolicyMapper.toAppliedPolicyResponse(familySub, policySubs, blockedServiceSubs);
    }
}
