package hotspot.user.familyReport.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.familyReport.controller.port.FindFamilyReportMembersService;
import hotspot.user.familyReport.controller.response.FamilyReportMemberResponse;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyReportMembersServiceImpl implements FindFamilyReportMembersService {

    private static final Map<FamilyRole, Integer> FAMILY_ROLE_ORDER = Map.of(
            FamilyRole.OWNER, 0,
            FamilyRole.PARENT, 1,
            FamilyRole.CHILD, 2
    );

    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public List<FamilyReportMemberResponse> findMembers(Long familyId) {
        return familySubscriptionRepository.findByFamilyId(familyId)
                .stream()
                .sorted((left, right) -> {
                    int roleCompare = Integer.compare(
                            FAMILY_ROLE_ORDER.getOrDefault(left.getFamilyRole(), Integer.MAX_VALUE),
                            FAMILY_ROLE_ORDER.getOrDefault(right.getFamilyRole(), Integer.MAX_VALUE)
                    );

                    if (roleCompare != 0) {
                        return roleCompare;
                    }

                    return Long.compare(
                            left.getSubscription().getId(),
                            right.getSubscription().getId()
                    );
                })
                .map(familySubscription -> FamilyReportMemberResponse.builder()
                        .subId(familySubscription.getSubscription().getId())
                        .name(familySubscription.getSubscription().getMember().getName())
                        .familyRole(familySubscription.getFamilyRole())
                        // TODO: batch DB weekly_report 연동 후 이번 주 reportId 세팅
                        .reportId(null)
                        .build())
                .toList();
    }
}
