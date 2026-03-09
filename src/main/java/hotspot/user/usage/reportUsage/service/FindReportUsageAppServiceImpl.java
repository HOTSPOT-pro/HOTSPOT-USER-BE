package hotspot.user.usage.reportUsage.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageAppDayService;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageAppMonthService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;
import hotspot.user.usage.reportUsage.domain.AppUsage;
import hotspot.user.usage.reportUsage.domain.mapper.ReportUsageAppMapper;
import hotspot.user.usage.reportUsage.service.port.ReportUsageAppRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindReportUsageAppServiceImpl
        implements FindReportUsageAppMonthService, FindReportUsageAppDayService {

    private final SubscriptionService subscriptionService;
    private final ReportUsageAppRepository reportUsageAppRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;

    @Transactional(readOnly = true)
    @Override
    public ReportUsageAppResponse findReportUsageAppMonth(Long memberId, Long targetSubId) {

        Subscription subscription =
                subscriptionService.findByMemberId(memberId);

        // Redis 조회
        List<AppUsage> appUsages =
                reportUsageAppRepository
                        .findMonthlyAppUsage(targetSubId);

        // appId 목록 추출
        List<Long> appIds =
                appUsages.stream()
                        .map(AppUsage::getAppId)
                        .toList();

        // DB 조회
        List<AppBlockedService> services =
                appBlockedServiceRepository
                        .findAllByAppBlockedServiceIds(appIds);

        // Mapper 위임
        return ReportUsageAppMapper.toReportUsageAppResponse(
                LocalDateTime.now(),
                appUsages,
                ReportUsageAppMapper.toAppIdNameMap(services)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ReportUsageAppResponse findReportUsageAppDay(Long memberId, Long targetSubId, LocalDate date) {

        Subscription subscription =
                subscriptionService.findByMemberId(memberId);

        List<AppUsage> appUsages =
                reportUsageAppRepository
                        .findDailyAppUsage(targetSubId, date);

        List<Long> appIds =
                appUsages.stream()
                        .map(AppUsage::getAppId)
                        .toList();

        List<AppBlockedService> services =
                appBlockedServiceRepository
                        .findAllByAppBlockedServiceIds(appIds);

        return ReportUsageAppMapper.toReportUsageAppResponse(
                LocalDateTime.now(),
                appUsages,
                ReportUsageAppMapper.toAppIdNameMap(services)
        );
    }
}
