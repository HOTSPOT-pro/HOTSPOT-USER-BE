package hotspot.user.familyReport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.DayOfWeek;
import java.util.List;

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
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class FindFamilyReportMembersServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private FamilyReportRepository familyReportRepository;

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
        given(familyReportRepository.findByFamilyId(1L))
                .willReturn(java.util.Optional.of(FamilyReport.builder()
                        .id(10L)
                        .family(Family.builder().id(1L).build())
                        .receiveDay(DayOfWeek.WEDNESDAY)
                        .isActive(true)
                        .build()));

        FamilyReportMembersResponse result = service.findMembers(1L);

        assertThat(result.receiveDay()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(result.members()).hasSize(3);
        assertThat(result.members().get(0).subId()).isEqualTo(10L);
        assertThat(result.members().get(0).name()).isEqualTo("홍길동");
        assertThat(result.members().get(0).familyRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(result.members().get(0).reportId()).isNull();
        assertThat(result.members().get(1).subId()).isEqualTo(11L);
        assertThat(result.members().get(1).name()).isEqualTo("김철수");
        assertThat(result.members().get(1).familyRole()).isEqualTo(FamilyRole.PARENT);
        assertThat(result.members().get(1).reportId()).isNull();
        assertThat(result.members().get(2).subId()).isEqualTo(12L);
        assertThat(result.members().get(2).name()).isEqualTo("김영희");
        assertThat(result.members().get(2).familyRole()).isEqualTo(FamilyRole.CHILD);
        assertThat(result.members().get(2).reportId()).isNull();
    }
}
