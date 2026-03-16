package hotspot.user.familyReport.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.familyReport.controller.port.FindFamilyReportMembersService;
import hotspot.user.familyReport.controller.response.FamilyReportMemberResponse;
import hotspot.user.familyReport.controller.response.FamilyReportMembersResponse;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
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
    private final FamilyReportRepository familyReportRepository;

    @Override
    public FamilyReportMembersResponse findMembers(Long familyId) {
        List<FamilyReportMemberResponse> members = familySubscriptionRepository.findByFamilyId(familyId)
                .stream()
                .sorted(Comparator
                        .comparing((hotspot.user.family.domain.FamilySubscription familySubscription) ->
                                FAMILY_ROLE_ORDER.getOrDefault(
                                        familySubscription.getFamilyRole(),
                                        Integer.MAX_VALUE
                                ))
                        .thenComparing(familySubscription -> familySubscription.getSubscription().getId()))
                .map(familySubscription -> FamilyReportMemberResponse.builder()
                        .subId(familySubscription.getSubscription().getId())
                        .name(familySubscription.getSubscription().getMember().getName())
                        .familyRole(familySubscription.getFamilyRole())
                        // batch DB weekly_report 연동 후 이번 주 reportId 세팅 예정
                        .reportId(null)
                        .build())
                .toList();

        return FamilyReportMembersResponse.builder()
                .receiveDay(familyReportRepository.findByFamilyId(familyId)
                        .filter(hotspot.user.familyReport.domain.FamilyReport::isActive)
                        .map(hotspot.user.familyReport.domain.FamilyReport::getReceiveDay)
                        .orElse(null))
                .members(members)
                .build();
    }
}
