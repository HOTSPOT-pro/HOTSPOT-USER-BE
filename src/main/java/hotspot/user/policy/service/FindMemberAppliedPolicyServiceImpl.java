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
@Transactional
public class FindMemberAppliedPolicyServiceImpl implements FindMemberAppliedPolicyService {

    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final PolicySubRepository policySubRepository;
    private final BlockedServiceSubRepository blockedServiceSubRepository;
    private final BlockPolicyRepository blockPolicyRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;
    private final FindBlockStatusService findBlockStatusService;

    @Override
    public AppliedPolicyResponse findByMemberId(Long memberId) {
        FamilySubscription familySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        Long subId = familySub.getSubscription().getId();

        // 1. DB에서 활성 정책 매핑만 조회
        List<PolicySub> policySubs = policySubRepository.findActiveBySubId(subId);

        // 2. 정책 상세 정보 조회 (N+1 방지)
        List<Long> policyIds = policySubs.stream().map(PolicySub::getBlockPolicyId).toList();
        Map<Long, BlockPolicy> policyMap = blockPolicyRepository.findAllById(policyIds).stream()
                .collect(Collectors.toMap(BlockPolicy::getId, p -> p));

        // 3. 일회성(ONCE) 정책 만료 체크 (Lazy Deactivation - 도메인 위임)
        List<PolicySub> expiredPolicySubs = new ArrayList<>();
        for (PolicySub sub : policySubs) {
            if (sub.deactivateIfExpired(policyMap.get(sub.getBlockPolicyId()))) {
                expiredPolicySubs.add(sub);
            }
        }

        // 4. 만료되어 상태가 변경된 정책이 있다면 DB 반영
        if (!expiredPolicySubs.isEmpty()) {
            policySubRepository.saveAll(expiredPolicySubs);
        }

        // 5. 활성화된 정책 리스트 필터링 (최종 응답용 - 순수 함수형 스트림)
        List<PolicySub> activePolicySubs = policySubs.stream()
                .filter(PolicySub::isActive)
                .toList();

        // 6. 활성 앱 차단 서비스 조회
        List<BlockedServiceSub> blockedServiceSubs = blockedServiceSubRepository.findActiveBySubId(subId);
        List<Long> appBlockedServiceIds = blockedServiceSubs.stream()
                .map(BlockedServiceSub::getAppBlockedServiceId)
                .toList();
        Map<Long, AppBlockedService> appBlockedServiceMap = appBlockedServiceRepository
                .findAllByAppBlockedServiceIds(appBlockedServiceIds).stream()
                .collect(Collectors.toMap(AppBlockedService::getId, s -> s));

        // 7. 실시간 차단 여부 조회
        BlockedStatusResponse blockStatus = findBlockStatusService.findMyBlockStatus(memberId);

        // 즉시 차단 / 정책에 의한 차단 둘 중 하나만 해당되도 차단되었다고 표시
        boolean isBlocked = blockStatus.isCurrentlyBlocked() || blockStatus.isImmediateBlocked();

        // 매퍼의 통합 조립 메서드 호출 (만료된 정책 제외하고 전달)
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
