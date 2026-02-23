package hotspot.user.usage.reportUsage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.ReportUsageErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;
import hotspot.user.usage.reportUsage.service.port.ReportUsageRepository;

@ExtendWith(MockitoExtension.class)
class FindReportUsageDayServiceImplTest {

    @Mock
    private ReportUsageRepository reportUsageRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private Clock clock;
    private FindReportUsageDayServiceImpl service;

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

        service = new FindReportUsageDayServiceImpl(
                reportUsageRepository,
                familySubscriptionRepository,
                subscriptionService,
                clock
        );
    }

    @Test
    @DisplayName("일별 리포트 조회 성공")
    void shouldReturnReportUsageDaySuccessfully() {

        Subscription selfSubscription = mock(Subscription.class);
        when(selfSubscription.getId()).thenReturn(selfSubId);
        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(selfSubscription);

        FamilySubscription selfFamilySub =
                mockFamilySubscription(selfSubId, "본인");

        FamilySubscription targetFamilySub =
                mockFamilySubscription(targetSubId, "가족");

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(selfFamilySub, targetFamilySub));

        Map<Long, Map<LocalDate, Double>> redisResult =
                new LinkedHashMap<>();

        redisResult.put(selfSubId, Map.of());
        redisResult.put(targetSubId, Map.of());

        when(reportUsageRepository.findReportUsageDailyGb(
                anyList(), anyList()
        )).thenReturn(redisResult);

        ReportUsageDayResponse response =
                service.findReportUsageDay(
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

        Subscription selfSubscription = mock(Subscription.class);
        when(selfSubscription.getId()).thenReturn(selfSubId);
        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(selfSubscription);

        FamilySubscription selfFamilySub =
                mockFamilySubscription(selfSubId, "본인");

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(selfFamilySub));

        when(reportUsageRepository.findReportUsageDailyGb(
                anyList(), anyList()
        )).thenReturn(Map.of());

        ReportUsageDayResponse response =
                service.findReportUsageDay(
                        memberId,
                        familyId,
                        null
                );

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("targetSubId가 가족에 없으면 예외 발생")
    void shouldThrowExceptionWhenTargetNotInFamily() {

        Subscription selfSubscription = mock(Subscription.class);
        when(selfSubscription.getId()).thenReturn(selfSubId);
        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(selfSubscription);

        FamilySubscription selfFamilySub =
                mockFamilySubscription(selfSubId, "본인");

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(selfFamilySub));

        assertThatThrownBy(() ->
                service.findReportUsageDay(
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
