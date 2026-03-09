package hotspot.user.policy.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import hotspot.user.policy.infrastructure.schema.FamilyDataControl;
import hotspot.user.policy.service.port.FamilyDataLimitRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyAppliedPolicyServiceImpl implements FindFamilyAppliedPolicyService {

    private final FamilyRepository familyRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FindMemberAppliedPolicyService findMemberAppliedPolicyService;
    private final FamilyDataLimitRepository familyDataLimitRepository;

    @Override
    public FamilyAppliedPolicyResponse findByFamilyId(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        List<FamilySubscription> memberMappings =
                familySubscriptionRepository.findByFamilyId(familyId);

        // Redis 조회
        FamilyDataControl familyDataControl =
                familyDataLimitRepository.findFamilyDataLimit(familyId);

        Map<Long, FamilyDataControl.SubFamilyDataControl> redisMap =
                familyDataControl.subFamilies().stream()
                        .collect(Collectors.toMap(
                                FamilyDataControl.SubFamilyDataControl::subId,
                                it -> it
                        ));

        List<AppliedPolicyResponse> memberPolicies =
                memberMappings.stream()
                        .map(mapping -> {

                            Long memberId =
                                    mapping.getSubscription().getMember().getId();

                            AppliedPolicyResponse base =
                                    findMemberAppliedPolicyService.findByMemberId(memberId);

                            FamilyDataControl.SubFamilyDataControl redis =
                                    redisMap.get(base.subId());

                            double limit = 0;
                            double usage = 0;

                            if (redis != null) {
                                limit = redis.familyDataSubLimit();
                                usage = redis.familyDataUsage();
                            }

                            return AppliedPolicyMapper.mergeRedisUsage(
                                    base,
                                    limit,
                                    usage
                            );
                        })
                        .toList();

        double familyLimitGb = familyDataControl.familyDataLimit();

        return AppliedPolicyMapper.toFamilyAppliedPolicyResponse(
                family,
                memberPolicies,
                familyLimitGb
        );
    }
}
