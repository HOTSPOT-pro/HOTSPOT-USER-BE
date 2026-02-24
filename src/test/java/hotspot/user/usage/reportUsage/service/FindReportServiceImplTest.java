package hotspot.user.usage.reportUsage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindReportServiceImplTest {

    @Mock
    FamilySubscriptionRepository familySubscriptionRepository;

    @InjectMocks
    FindReportServiceImpl service;

    @Test
    @DisplayName("가족 구성원 조회 성공")
    void shouldReturnFamilyReportSuccessfully() {

        Long familyId = 1L;

        Member member = Member.builder()
                .id(10L)
                .name("홍길동")
                .build();

        Subscription subscription = Subscription.builder()
                .id(100L)
                .member(member)
                .build();

        Family family = Family.builder()
                .id(familyId)
                .build();

        FamilySubscription familySubscription =
                FamilySubscription.builder()
                        .id(1L)
                        .subscription(subscription)
                        .family(family)
                        .familyRole(FamilyRole.OWNER)
                        .priority(1)
                        .dataLimit(10000)
                        .build();

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(familySubscription));

        List<ReportFamilyResponse> result =
                service.findReportFamily(familyId);
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).subId()).isEqualTo(100L);
        assertThat(result.get(0).subName()).isEqualTo("홍길동");
    }
}