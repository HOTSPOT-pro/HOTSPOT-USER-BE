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

    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FamilyReportRepository familyReportRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final Clock clock;

    @Override
    public FamilyReportMembersResponse findMembers(Long familyId) {
        // 1. 조회 후 CHILD 역할만 필터링
        List<FamilySubscription> childSubscriptions =
                familySubscriptionRepository.findByFamilyId(familyId).stream()
                        .filter(fs -> fs.getFamilyRole() == FamilyRole.CHILD)
                        .toList();

        Optional<DayOfWeek> receiveDay = familyReportRepository.findActiveReceiveDayByFamilyId(familyId);

        LocalDate currentWeekStartDate = getCurrentWeekStartDate();
        LocalDate currentWeekEndDate = currentWeekStartDate.plusDays(6);

        // 2. 필터링된 CHILD 목록의 ID로 리포트 ID 조회
        Map<Long, Long> weeklyReportIdBySubId = weeklyReportRepository.findCompletedCurrentWeekReportIdsBySubIds(
                childSubscriptions.stream()
                        .map(fs -> fs.getSubscription().getId())
                        .toList(),
                currentWeekStartDate,
                currentWeekEndDate
        );

        // 3. 응답 객체 매핑 (모두 CHILD이므로 ID 오름차순 정렬만 수행)
        List<FamilyReportMemberResponse> members = childSubscriptions
                .stream()
                .sorted(Comparator.comparing(fs -> fs.getSubscription().getId()))
                .map(fs -> FamilyReportMemberResponse.builder()
                        .subId(fs.getSubscription().getId())
                        .name(fs.getSubscription().getMember().getName())
                        .familyRole(fs.getFamilyRole())
                        .reportId(weeklyReportIdBySubId.get(fs.getSubscription().getId()))
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