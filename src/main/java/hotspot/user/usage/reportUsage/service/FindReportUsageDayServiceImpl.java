package hotspot.user.usage.reportUsage.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.ReportUsageErrorCode;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageDayService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;
import hotspot.user.usage.reportUsage.domain.mapper.ReportUsageDayMapper;
import hotspot.user.usage.reportUsage.service.port.ReportUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindReportUsageDayServiceImpl implements FindReportUsageDayService {

    private final ReportUsageRepository reportUsageRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public ReportUsageDayResponse findReportUsageDay(
            Long familyId,
            Long targetSubId
    ) {

        List<FamilySubList> familySubList;

        if (familyId == null) {

            familySubList = List.of(
                    new FamilySubList(targetSubId, null) // 이름은 mapper에서 처리하거나 null 허용
            );

        } else {

            familySubList =
                    familySubscriptionRepository.findByFamilyId(familyId)
                            .stream()
                            .map(FamilyUsageMapper::toFamilySubList)
                            .toList();

            validateTargetInFamily(familySubList, targetSubId);
        }

        List<Long> subIds = familySubList.stream()
                .map(FamilySubList::subId)
                .toList();

        List<LocalDate> dates = generateMonthDates();

        Map<Long, Map<LocalDate, Double>> subDailyMap =
                reportUsageRepository
                        .findReportUsageDailyGb(subIds, dates);

        return ReportUsageDayMapper.toReportUsageDayResponse(
                LocalDateTime.now(clock),
                dates,
                familySubList,
                subDailyMap,
                targetSubId
        );
    }

    private void validateTargetInFamily(
            List<FamilySubList> familySubList,
            Long targetSubId
    ) {
        if (targetSubId == null) {
            return;
        }

        boolean exists =
                familySubList.stream()
                        .anyMatch(f -> f.subId().equals(targetSubId));

        if (!exists) {
            throw new ApplicationException(
                    ReportUsageErrorCode.TARGET_SUBSCRIPTION_NOT_IN_FAMILY
            );
        }
    }

    private List<LocalDate> generateMonthDates() {

        LocalDate today = LocalDate.now(clock);
        LocalDate start = today.withDayOfMonth(1);

        return start.datesUntil(today.plusDays(1)).toList();
    }
}
