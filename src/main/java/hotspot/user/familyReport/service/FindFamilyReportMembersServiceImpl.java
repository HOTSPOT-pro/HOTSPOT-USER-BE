package hotspot.user.familyReport.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.familyReport.controller.port.FindFamilyReportMembersService;
import hotspot.user.familyReport.controller.response.FamilyReportMemberResponse;
import hotspot.user.familyReport.controller.response.FamilyReportMembersResponse;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;
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
    private final WeeklyReportRepository weeklyReportRepository;
    private final Clock clock;

    @Override
    public FamilyReportMembersResponse findMembers(Long familyId) {
        List<FamilySubscription> familySubscriptions =
                familySubscriptionRepository.findByFamilyId(familyId);

        Optional<DayOfWeek> receiveDay = familyReportRepository.findActiveReceiveDayByFamilyId(familyId);

        LocalDate currentWeekStartDate = getCurrentWeekStartDate();
        LocalDate currentWeekEndDate = currentWeekStartDate.plusDays(6);

        Map<Long, Long> weeklyReportIdBySubId = weeklyReportRepository.findCompletedCurrentWeekReportIdsBySubIds(
                familySubscriptions.stream()
                        .map(familySubscription -> familySubscription.getSubscription().getId())
                        .toList(),
                currentWeekStartDate,
                currentWeekEndDate
        );

        List<FamilyReportMemberResponse> members = familySubscriptions
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
                        .reportId(weeklyReportIdBySubId.get(familySubscription.getSubscription().getId()))
                        .build())
                .toList();

        return FamilyReportMembersResponse.builder()
                .receiveDay(receiveDay.orElse(null))
                .members(members)
                .build();
    }

    private LocalDate getCurrentWeekStartDate() {
        LocalDate today = LocalDate.now(clock);
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
