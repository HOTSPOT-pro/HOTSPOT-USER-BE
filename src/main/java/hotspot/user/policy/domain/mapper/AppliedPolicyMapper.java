package hotspot.user.policy.domain.mapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;

/**
 * 구성원별 적용된 정책 <-> Dto 변환하는 매퍼 클래스
 */

public class AppliedPolicyMapper {

    public static AppliedPolicyResponse toAppliedPolicyResponse(
            FamilySubscription familySub,
            List<PolicySub> policySubs,
            List<BlockedServiceSub> blockedServiceSubs,
            Map<Long, BlockPolicy> policyMap,
            Map<Long, AppBlockedService> appBlockedServiceMap,
            boolean isBlocked
    ) {

        return AppliedPolicyResponse.builder()
                .memberId(familySub.getSubscription().getMember().getId())
                .memberName(familySub.getSubscription().getMember().getName())
                .subId(familySub.getSubscription().getId())
                .role(familySub.getFamilyRole())
                .priority(familySub.getPriority())
                .familyDataSubLimit(0)
                .familyDataUsage(0)
                .isBlocked(isBlocked)
                .blockPolicyResponseList(policySubs.stream()
                        .map(sub -> PolicySubMapper.toBlockPolicyResponse(
                                sub,
                                policyMap.get(sub.getBlockPolicyId())))
                        .filter(Objects::nonNull)
                        .toList())
                .appBlockedServiceResponseList(blockedServiceSubs.stream()
                        .map(sub -> appBlockedServiceMap.get(sub.getAppBlockedServiceId()))
                        .filter(Objects::nonNull)
                        .map(AppBlockedServiceMapper::toAppBlockedServiceResponse)
                        .toList())
                .build();
    }

    public static FamilyAppliedPolicyResponse toFamilyAppliedPolicyResponse(
            Family family,
            List<AppliedPolicyResponse> memberPolicies,
            double familyDataAmount
    ) {
        return FamilyAppliedPolicyResponse.builder()
                .familyId(family.getId())
                .familyNum(family.getFamilyNum())
                .familyDataAmount(familyDataAmount)
                .priorityType(family.getPriorityType())
                .memberPolicies(memberPolicies)
                .build();
    }

    public static AppliedPolicyResponse mergeRedisUsage(
            AppliedPolicyResponse base,
            double familyLimit,
            double familyUsage
    ) {

        return AppliedPolicyResponse.builder()
                .memberId(base.memberId())
                .memberName(base.memberName())
                .subId(base.subId())
                .role(base.role())
                .priority(base.priority())
                .isBlocked(base.isBlocked())
                .familyDataSubLimit(familyLimit)
                .familyDataUsage(familyUsage)
                .blockPolicyResponseList(base.blockPolicyResponseList())
                .appBlockedServiceResponseList(base.appBlockedServiceResponseList())
                .build();
    }
}
