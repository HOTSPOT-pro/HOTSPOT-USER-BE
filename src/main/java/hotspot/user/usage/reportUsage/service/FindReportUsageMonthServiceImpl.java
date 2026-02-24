package hotspot.user.usage.reportUsage.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.ReportUsageErrorCode;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageMonthService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;
import hotspot.user.usage.reportUsage.domain.mapper.ReportUsageMonthMapper;
import hotspot.user.usage.reportUsage.service.port.ReportUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindReportUsageMonthServiceImpl
        implements FindReportUsageMonthService {

    private final ReportUsageRepository reportUsageRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public ReportUsageMonthResponse findReportUsageMonth(
            Long memberId,
            Long familyId,
            Long targetSubId
    ) {

        Long selfSubId =
                subscriptionService.findByMemberId(memberId).getId();

        List<FamilySubList> familySubList;

        if (familyId == null) {

            // 🔒 가족이 없는 사용자 → 본인만 조회 가능
            if (targetSubId != null && !targetSubId.equals(selfSubId)) {
                throw new ApplicationException(
                        ReportUsageErrorCode.TARGET_SUBSCRIPTION_NOT_IN_FAMILY
                );
            }

            familySubList = List.of(
                    new FamilySubList(selfSubId, null)
            );

        } else {

            familySubList =
                    familySubscriptionRepository.findByFamilyId(familyId)
                            .stream()
                            .map(FamilyUsageMapper::toFamilySubList)
                            .toList();

            validateTargetInFamily(familySubList, targetSubId);
        }

        List<Long> subIds =
                familySubList.stream()
                        .map(FamilySubList::subId)
                        .toList();

        List<YearMonth> months = generateLastSixMonths();

        Map<Long, Map<YearMonth, Double>> subMonthlyMap =
                reportUsageRepository.findReportUsageMonthlyGb(
                        subIds,
                        months
                );

        return ReportUsageMonthMapper.toReportUsageMonthResponse(
                LocalDateTime.now(clock),
                months,
                familySubList,
                subMonthlyMap,
                selfSubId,
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

    private List<YearMonth> generateLastSixMonths() {

        YearMonth current = YearMonth.now(clock);

        List<YearMonth> months = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }

        return months;
    }
}
