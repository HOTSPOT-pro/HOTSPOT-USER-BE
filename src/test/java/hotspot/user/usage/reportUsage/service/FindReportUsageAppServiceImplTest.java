package hotspot.user.usage.reportUsage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;
import hotspot.user.usage.reportUsage.domain.AppUsage;
import hotspot.user.usage.reportUsage.service.port.ReportUsageAppRepository;

@ExtendWith(MockitoExtension.class)
class FindReportUsageAppServiceImplTest {

    @Mock
    SubscriptionService subscriptionService;

    @Mock
    ReportUsageAppRepository reportUsageAppRepository;

    @Mock
    AppBlockedServiceRepository appBlockedServiceRepository;

    @InjectMocks
    FindReportUsageAppServiceImpl service;

    private final Long memberId = 1L;
    private final Long subId = 10L;

    @Test
    void shouldReturnMonthlyAppUsage() {

        List<AppUsage> usages = List.of(
                new AppUsage(1L, 3.5),
                new AppUsage(2L, 1.2)
        );

        when(reportUsageAppRepository.findMonthlyAppUsage(subId))
                .thenReturn(usages);

        List<AppBlockedService> services = List.of(
                AppBlockedService.builder()
                        .id(1L)
                        .name("YouTube")
                        .serviceCode("YT")
                        .build(),
                AppBlockedService.builder()
                        .id(2L)
                        .name("Netflix")
                        .serviceCode("NF")
                        .build()
        );

        when(appBlockedServiceRepository
                .findAllByAppBlockedServiceIds(List.of(1L, 2L)))
                .thenReturn(services);

        ReportUsageAppResponse response =
                service.findReportUsageAppMonth(memberId, subId);

        assertThat(response.appUsages()).hasSize(2);
        assertThat(response.appUsages().get(0).appName())
                .isEqualTo("YouTube");

        verify(reportUsageAppRepository).findMonthlyAppUsage(subId);
        verify(appBlockedServiceRepository)
                .findAllByAppBlockedServiceIds(List.of(1L, 2L));
    }

    @Test
    void shouldReturnDailyAppUsage() {

        List<AppUsage> usages = List.of(
                new AppUsage(3L, 2.0)
        );

        when(reportUsageAppRepository.findDailyAppUsage(subId, LocalDate.now()))
                .thenReturn(usages);

        List<AppBlockedService> services = List.of(
                AppBlockedService.builder()
                        .id(3L)
                        .name("Instagram")
                        .serviceCode("IS")
                        .build()
        );

        when(appBlockedServiceRepository
                .findAllActiveAndInDeleteByAppBlockedServiceIds(List.of(3L)))
                .thenReturn(services);

        ReportUsageAppResponse response =
                service.findReportUsageAppDay(memberId, subId, LocalDate.now());

        assertThat(response.appUsages()).hasSize(1);
        assertThat(response.appUsages().get(0).appName())
                .isEqualTo("Instagram");

        verify(reportUsageAppRepository)
                .findDailyAppUsage(subId, LocalDate.now());

        verify(appBlockedServiceRepository)
                .findAllActiveAndInDeleteByAppBlockedServiceIds(List.of(3L));
    }
}
