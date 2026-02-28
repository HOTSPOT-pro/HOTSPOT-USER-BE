package hotspot.user.policy.domain.mapper;

import java.util.List;

import hotspot.user.common.util.redis.RedisUsageCalculator;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.domain.PolicySub;

import java.util.Map;

/**
 * 구성원별 적용된 정책 <-> Dto 변환하는 매퍼 클래스
 */
public class AppliedPolicyMapper {

    // 개인별 정책 정보를 조립하여 AppliedPolicyResponse DTO를 생성
    public static AppliedPolicyResponse toAppliedPolicyResponse(
            FamilySubscription familySub,
            List<PolicySub> policySubs,
            List<BlockedServiceSub> blockedServiceSubs,
            Map<Long, BlockPolicy> policyMap
    ) {
        return AppliedPolicyResponse.builder()
                .memberId(familySub.getSubscription().getMember().getId())
                .memberName(familySub.getSubscription().getMember().getName())
                .subId(familySub.getSubscription().getId())
                .dataLimit(RedisUsageCalculator.kbToGb(familySub.getDataLimit()))
                .priority(familySub.getPriority())
                .blockPolicyResponseList(policySubs.stream()
                        .map(sub -> PolicySubMapper.toBlockPolicyResponse(sub, policyMap.get(sub.getBlockPolicyId())))
                        .toList())
                .appBlockedServiceResponseList(blockedServiceSubs.stream()
                        .map(AppBlockedServiceMapper::toAppBlockedServiceResponse)
                        .toList())
                .build();
    }

    // 가족 정보와 조립된 멤버별 정책 리스트를 사용하여 FamilyAppliedPolicyResponse DTO를 생성
    public static FamilyAppliedPolicyResponse toFamilyAppliedPolicyResponse(
            Family family,
            List<AppliedPolicyResponse> memberPolicies
    ) {
        return FamilyAppliedPolicyResponse.builder()
                .familyId(family.getId())
                .familyNum(family.getFamilyNum())
                .familyDataAmount(RedisUsageCalculator.kbToGb(family.getFamilyDataAmount()))
                .priorityType(family.getPriorityType())
                .memberPolicies(memberPolicies)
                .build();
    }
}
