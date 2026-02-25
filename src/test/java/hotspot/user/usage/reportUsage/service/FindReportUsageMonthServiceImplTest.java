package hotspot.user.usage.reportUsage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import hotspot.user.member.domain.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;
import hotspot.user.usage.reportUsage.service.port.ReportUsageRepository;

@ExtendWith(MockitoExtension.class)
class FindReportUsageMonthServiceImplTest {

    @Mock private ReportUsageRepository reportUsageRepository;
    @Mock private FamilySubscriptionRepository familySubscriptionRepository;

    private Clock clock;
    private FindReportUsageMonthServiceImpl service;

    private final Long familyId = 100L;
    private final Long sub1 = 10L;
    private final Long sub2 = 20L;

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
                clock
        );
    }

    @Test
    void shouldReturnSuccessfully() {

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(
                        mockFamily(sub1, "본인"),
                        mockFamily(sub2, "가족")
                ));

        when(reportUsageRepository.findReportUsageMonthlyGb(anyList(), anyList()))
                .thenReturn(Map.of());

        ReportUsageMonthResponse response =
                service.findReportUsageMonth(familyId, sub2);

        assertThat(response.subUsages()).hasSize(2);
    }

    @Test
    void shouldThrowWhenTargetNotInFamily() {

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(mockFamily(sub1, "본인")));

        assertThatThrownBy(() ->
                service.findReportUsageMonth(familyId, sub2)
        ).isInstanceOf(ApplicationException.class);
    }

    private FamilySubscription mockFamily(Long subId, String name) {

        Member member = Member.builder()
                .id(1L)
                .name(name)
                .birth("000101")
                .status(Status.APPROVED)
                .build();

        Subscription subscription = Subscription.builder()
                .id(subId)
                .member(member)
                .plan(null)
                .phoneEnc(null)
                .phoneHash(null)
                .isLocked(false)
                .build();

        return FamilySubscription.builder()
                .id(1L)
                .subscription(subscription)
                .family(null)
                .familyRole(null)
                .priority(0)
                .dataLimit(0)
                .build();
    }
}
