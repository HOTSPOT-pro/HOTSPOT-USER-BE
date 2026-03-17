package hotspot.user.familyReport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.familyReport.controller.response.FamilyReportMembersResponse;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;

@ExtendWith(MockitoExtension.class)
class FindFamilyReportMembersServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private FamilyReportRepository familyReportRepository;

    @Mock
    private WeeklyReportRepository weeklyReportRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private FindFamilyReportMembersServiceImpl service;

    @Test
    @DisplayName("가족 구성원 목록은 대표, 부모, 자녀 순으로 조회된다")
    void findMembersSuccess() {
        given(familySubscriptionRepository.findByFamilyId(1L))
                .willReturn(List.of(
                        FamilySubscription.builder()
                                .family(Family.builder().id(1L).build())
                                .subscription(Subscription.builder()
                                        .id(12L)
                                        .member(Member.builder().id(3L).name("김영희").build())
                                        .build())
                                .familyRole(FamilyRole.CHILD)
                                .build(),
                        FamilySubscription.builder()
                                .family(Family.builder().id(1L).build())
                                .subscription(Subscription.builder()
                                        .id(11L)
                                        .member(Member.builder().id(2L).name("김철수").build())
                                        .build())
                                .familyRole(FamilyRole.PARENT)
                                .build(),
                        FamilySubscription.builder()
                                .family(Family.builder().id(1L).build())
                                .subscription(Subscription.builder()
                                        .id(10L)
                                        .member(Member.builder().id(1L).name("홍길동").build())
                                        .build())
                                .familyRole(FamilyRole.OWNER)
                                .build()
        ));
        given(familyReportRepository.findActiveReceiveDayByFamilyId(1L))
                .willReturn(Optional.of(DayOfWeek.WEDNESDAY));
        given(clock.instant()).willReturn(Instant.parse("2026-03-19T00:00:00Z"));
        given(clock.getZone()).willReturn(java.time.ZoneId.of("Asia/Seoul"));
        given(weeklyReportRepository.findCompletedCurrentWeekReportIdsBySubIds(
                List.of(12L, 11L, 10L),
                java.time.LocalDate.of(2026, 3, 16),
                java.time.LocalDate.of(2026, 3, 22)))
                .willReturn(Map.of(
                        10L, 1001L,
                        11L, 1002L
                ));

        FamilyReportMembersResponse result = service.findMembers(1L);

        assertThat(result.receiveDay()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(result.members()).hasSize(3);
        assertThat(result.members().get(0).subId()).isEqualTo(10L);
        assertThat(result.members().get(0).name()).isEqualTo("홍길동");
        assertThat(result.members().get(0).familyRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(result.members().get(0).reportId()).isEqualTo(1001L);
        assertThat(result.members().get(1).subId()).isEqualTo(11L);
        assertThat(result.members().get(1).name()).isEqualTo("김철수");
        assertThat(result.members().get(1).familyRole()).isEqualTo(FamilyRole.PARENT);
        assertThat(result.members().get(1).reportId()).isEqualTo(1002L);
        assertThat(result.members().get(2).subId()).isEqualTo(12L);
        assertThat(result.members().get(2).name()).isEqualTo("김영희");
        assertThat(result.members().get(2).familyRole()).isEqualTo(FamilyRole.CHILD);
        assertThat(result.members().get(2).reportId()).isNull();
    }

    @Test
    @DisplayName("현재 수신 요일이 바뀌어도 이번 주 생성된 리포트가 있으면 reportId를 내려준다")
    void findMembersWithChangedReceiveDay() {
        given(familySubscriptionRepository.findByFamilyId(1L))
                .willReturn(List.of(
                        FamilySubscription.builder()
                                .family(Family.builder().id(1L).build())
                                .subscription(Subscription.builder()
                                        .id(10L)
                                        .member(Member.builder().id(1L).name("홍길동").build())
                                        .build())
                                .familyRole(FamilyRole.OWNER)
                                .build()
                ));
        given(familyReportRepository.findActiveReceiveDayByFamilyId(1L))
                .willReturn(Optional.of(DayOfWeek.FRIDAY));
        given(clock.instant()).willReturn(Instant.parse("2026-03-19T00:00:00Z"));
        given(clock.getZone()).willReturn(java.time.ZoneId.of("Asia/Seoul"));
        given(weeklyReportRepository.findCompletedCurrentWeekReportIdsBySubIds(
                List.of(10L),
                java.time.LocalDate.of(2026, 3, 16),
                java.time.LocalDate.of(2026, 3, 22)))
                .willReturn(Map.of(10L, 1001L));

        FamilyReportMembersResponse result = service.findMembers(1L);

        assertThat(result.receiveDay()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(result.members()).hasSize(1);
        assertThat(result.members().get(0).reportId()).isEqualTo(1001L);
    }
}
