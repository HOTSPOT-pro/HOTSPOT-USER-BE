package hotspot.user.usage.reportUsage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import hotspot.user.subscription.domain.Subscription;
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

    @BeforeEach
    void setup() {
        Subscription subscription = mock(Subscription.class);
        when(subscription.getId()).thenReturn(subId);
        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(subscription);
    }

    @Test
    @DisplayName("월별 앱 사용량 조회 성공")
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
                .findAllByAppBlackedServiceIds(List.of(1L, 2L)))
                .thenReturn(services);

        ReportUsageAppResponse response =
                service.findReportUsageAppMonth(memberId);

        assertThat(response).isNotNull();
        assertThat(response.appUsages()).hasSize(2);
        assertThat(response.appUsages().get(0).appName())
                .isEqualTo("YouTube");
        assertThat(response.appUsages().get(0).appDataUsageAmount())
                .isEqualTo(3.5);

        verify(subscriptionService).findByMemberId(memberId);
        verify(reportUsageAppRepository).findMonthlyAppUsage(subId);
        verify(appBlockedServiceRepository)
                .findAllByAppBlackedServiceIds(List.of(1L, 2L));
    }

    @Test
    @DisplayName("일별 앱 사용량 조회 성공")
    void shouldReturnDailyAppUsage() {

        List<AppUsage> usages = List.of(
                new AppUsage(3L, 2.0)
        );

        when(reportUsageAppRepository.findDailyAppUsage(subId))
                .thenReturn(usages);

        List<AppBlockedService> services = List.of(
                AppBlockedService.builder()
                        .id(3L)
                        .name("Instagram")
                        .serviceCode("IS")
                        .build()
        );

        when(appBlockedServiceRepository
                .findAllByAppBlackedServiceIds(List.of(3L)))
                .thenReturn(services);

        ReportUsageAppResponse response =
                service.findReportUsageAppDay(memberId);

        assertThat(response.appUsages()).hasSize(1);
        assertThat(response.appUsages().get(0).appName())
                .isEqualTo("Instagram");
        assertThat(response.appUsages().get(0).appDataUsageAmount())
                .isEqualTo(2.0);

        verify(reportUsageAppRepository).findDailyAppUsage(subId);
    }
}
