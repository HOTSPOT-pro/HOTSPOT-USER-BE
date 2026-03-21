package hotspot.user.policy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.policy.controller.port.FindBlockStatusService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockedStatusResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.AppliedPolicyMapper;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
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
    private final AppBlockedServiceRepository appBlockedServiceRepository;
    private final FindBlockStatusService findBlockStatusService;

    @Override
    @Transactional
    public AppliedPolicyResponse findByMemberId(Long memberId) {

        FamilySubscription familySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        Long subId = familySub.getSubscription().getId();

        // 1. 활성 정책 매핑 조회
        List<PolicySub> policySubs = policySubRepository.findActiveBySubId(subId);

        // 2. 정책 상세 조회
        List<Long> policyIds = policySubs.stream()
                .map(PolicySub::getBlockPolicyId)
                .toList();

        Map<Long, BlockPolicy> policyMap =
                blockPolicyRepository.findAllById(policyIds).stream()
                        .collect(Collectors.toMap(BlockPolicy::getId, p -> p));

        // 3. ONCE 정책 만료 체크 + 비활성화 대상 수집
        List<PolicySub> expiredPolicySubs = new ArrayList<>();

        for (PolicySub sub : policySubs) {
            BlockPolicy blockPolicy = policyMap.get(sub.getBlockPolicyId());

            if (blockPolicy != null && sub.deactivateIfExpired(blockPolicy)) {
                expiredPolicySubs.add(sub);
            }
        }

        // 4. 만료된 정책 저장
        if (!expiredPolicySubs.isEmpty()) {
            policySubRepository.saveAll(expiredPolicySubs);
        }

        // 5. 최종 활성 정책만 응답에 포함
        List<PolicySub> activePolicySubs = policySubs.stream()
                .filter(PolicySub::isActive)
                .toList();

        // 6. 활성 앱 차단 서비스 조회
        List<BlockedServiceSub> blockedServiceSubs =
                blockedServiceSubRepository.findActiveBySubId(subId);

        List<Long> appIds = blockedServiceSubs.stream()
                .map(BlockedServiceSub::getAppBlockedServiceId)
                .toList();

        Map<Long, AppBlockedService> appBlockedServiceMap =
                appBlockedServiceRepository
                        .findAllActiveAndInDeleteByAppBlockedServiceIds(appIds).stream()
                        .collect(Collectors.toMap(AppBlockedService::getId, s -> s));

        // 7. 실시간 차단 여부
        BlockedStatusResponse blockStatus =
                findBlockStatusService.findMyBlockStatus(memberId);

        boolean isBlocked = blockStatus.isCurrentlyBlocked();

        return AppliedPolicyMapper.toAppliedPolicyResponse(
                familySub,
                activePolicySubs,
                blockedServiceSubs,
                policyMap,
                appBlockedServiceMap,
                isBlocked
        );
    }
}
