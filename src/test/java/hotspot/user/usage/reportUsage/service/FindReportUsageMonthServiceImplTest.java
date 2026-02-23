package hotspot.user.usage.reportUsage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.ReportUsageErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;
import hotspot.user.usage.reportUsage.service.port.ReportUsageRepository;

@ExtendWith(MockitoExtension.class)
class FindReportUsageMonthServiceImplTest {

    @Mock
    private ReportUsageRepository reportUsageRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private Clock clock;
    private FindReportUsageMonthServiceImpl service;

    private final Long memberId = 1L;
    private final Long familyId = 100L;
    private final Long selfSubId = 10L;
    private final Long targetSubId = 20L;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                LocalDate.of(2026, 2, 23)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        service = new FindReportUsageMonthServiceImpl(
                reportUsageRepository,
                familySubscriptionRepository,
                subscriptionService,
                clock
        );
    }

    @Test
    @DisplayName("월별 리포트 조회 성공")
    void shouldReturnReportUsageMonthSuccessfully() {

        mockSelfSubscription();
        mockFamilyMembers(true);

        when(reportUsageRepository.findReportUsageMonthlyGb(
                anyList(), anyList()
        )).thenReturn(Map.of());

        ReportUsageMonthResponse response =
                service.findReportUsageMonth(
                        memberId,
                        familyId,
                        targetSubId
                );

        assertThat(response).isNotNull();
        assertThat(response.subUsages()).hasSize(3);
    }

    @Test
    @DisplayName("targetSubId가 null이면 정상 동작")
    void shouldWorkWhenTargetIsNull() {

        mockSelfSubscription();
        mockFamilyMembers(false);

        when(reportUsageRepository.findReportUsageMonthlyGb(
                anyList(), anyList()
        )).thenReturn(Map.of());

        ReportUsageMonthResponse response =
                service.findReportUsageMonth(
                        memberId,
                        familyId,
                        null
                );

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("targetSubId가 가족에 없으면 예외 발생")
    void shouldThrowExceptionWhenTargetNotInFamily() {

        mockSelfSubscription();

        FamilySubscription selfFamily =
                mockFamilySubscription(selfSubId, "본인");

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(selfFamily));

        assertThatThrownBy(() ->
                service.findReportUsageMonth(
                        memberId,
                        familyId,
                        targetSubId
                )
        ).isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getCode())
                            .isEqualTo(
                                    ReportUsageErrorCode.TARGET_SUBSCRIPTION_NOT_IN_FAMILY
                            );
                });
    }

    @Test
    @DisplayName("최근 6개월이 Repository로 정확히 전달된다 (2026-02 기준)")
    void shouldPassCorrectLastSixMonthsToRepository() {

        mockSelfSubscription();
        mockFamilyMembers(false);

        when(reportUsageRepository.findReportUsageMonthlyGb(
                anyList(), anyList()
        )).thenReturn(Map.of());

        service.findReportUsageMonth(
                memberId,
                familyId,
                null
        );

        ArgumentCaptor<List<YearMonth>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(reportUsageRepository)
                .findReportUsageMonthlyGb(
                        anyList(),
                        captor.capture()
                );

        List<YearMonth> months = captor.getValue();

        assertThat(months).containsExactly(
                YearMonth.of(2025, 9),
                YearMonth.of(2025, 10),
                YearMonth.of(2025, 11),
                YearMonth.of(2025, 12),
                YearMonth.of(2026, 1),
                YearMonth.of(2026, 2)
        );
    }

    private void mockSelfSubscription() {
        Subscription selfSubscription = mock(Subscription.class);
        when(selfSubscription.getId()).thenReturn(selfSubId);
        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(selfSubscription);
    }

    private void mockFamilyMembers(boolean includeTarget) {

        FamilySubscription selfFamily =
                mockFamilySubscription(selfSubId, "본인");

        if (includeTarget) {
            FamilySubscription targetFamily =
                    mockFamilySubscription(targetSubId, "가족");

            when(familySubscriptionRepository.findByFamilyId(familyId))
                    .thenReturn(List.of(selfFamily, targetFamily));
        } else {
            when(familySubscriptionRepository.findByFamilyId(familyId))
                    .thenReturn(List.of(selfFamily));
        }
    }

    private FamilySubscription mockFamilySubscription(Long subId, String name) {

        Member member = mock(Member.class);
        when(member.getName()).thenReturn(name);

        Subscription subscription = mock(Subscription.class);
        when(subscription.getId()).thenReturn(subId);
        when(subscription.getMember()).thenReturn(member);

        FamilySubscription familySubscription =
                mock(FamilySubscription.class);

        when(familySubscription.getSubscription())
                .thenReturn(subscription);

        return familySubscription;
    }
}
