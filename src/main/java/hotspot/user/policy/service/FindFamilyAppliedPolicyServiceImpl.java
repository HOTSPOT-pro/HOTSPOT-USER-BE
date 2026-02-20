package hotspot.user.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.domain.mapper.AppliedPolicyMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyAppliedPolicyServiceImpl implements FindFamilyAppliedPolicyService {

    private final FamilyRepository familyRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FindMemberAppliedPolicyService findMemberAppliedPolicyService;

    @Override
    public FamilyAppliedPolicyResponse findByFamilyId(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        List<FamilySubscription> memberMappings = familySubscriptionRepository.findByFamilyId(familyId);

        List<AppliedPolicyResponse> memberPolicies = memberMappings.stream()
                .map(mapping -> findMemberAppliedPolicyService
                        .findByMemberId(mapping.getSubscription().getMember().getId()))
                .toList();

        // 매퍼의 통합 조립 메서드 호출
        return AppliedPolicyMapper.toFamilyAppliedPolicyResponse(family, memberPolicies);
    }
}
