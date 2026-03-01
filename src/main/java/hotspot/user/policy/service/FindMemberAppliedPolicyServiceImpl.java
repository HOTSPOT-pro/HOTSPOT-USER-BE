package hotspot.user.policy.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.AppliedPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
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
    private final BlockPolicyRepository blockPolicyRepository;

    @Override
    public AppliedPolicyResponse findByMemberId(Long memberId) {
        FamilySubscription familySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        Long subId = familySub.getSubscription().getId();

        // DB에서 활성 정책만 직접 조회 (성능 최적화)
        List<PolicySub> policySubs = policySubRepository.findActiveBySubId(subId);
        List<BlockedServiceSub> blockedServiceSubs = blockedServiceSubRepository.findBySubId(subId);

        // 정책 상세 정보 조회 (N+1 방지)
        List<Long> policyIds = policySubs.stream().map(PolicySub::getBlockPolicyId).toList();
        Map<Long, BlockPolicy> policyMap = blockPolicyRepository.findAllById(policyIds).stream()
                .collect(Collectors.toMap(BlockPolicy::getId, p -> p));

        // 매퍼의 통합 조립 메서드 호출
        return AppliedPolicyMapper.toAppliedPolicyResponse(familySub, policySubs, blockedServiceSubs, policyMap);
    }
}
