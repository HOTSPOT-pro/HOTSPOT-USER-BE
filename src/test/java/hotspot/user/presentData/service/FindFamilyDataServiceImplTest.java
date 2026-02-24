package hotspot.user.presentData.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.Member;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.presentData.controller.response.FamilyDataResponse;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.presentData.domain.mapper.FamilyDataMapper;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.subscription.service.port.SubscriptionRepository;


@ExtendWith(MockitoExtension.class)
class FindFamilyDataServiceImplTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private PresentDataRepository presentDataRepository;

    @Mock
    private FamilyDataMapper familyDataMapper;

    @InjectMocks
    private FindFamilyDataServiceImpl service;

    @Test
    void shouldReturnFamilyDataSuccessfully() {

        Long memberId = 100L;
        Long familyId = 10L;

        // self
        Subscription self = mock(Subscription.class);
        Plan selfPlan = mock(Plan.class);

        when(self.getId()).thenReturn(1L);
        when(self.getPlan()).thenReturn(selfPlan);
        when(selfPlan.getDataPeriod()).thenReturn(DataPeriod.MONTH);

        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(self);

        // sub2
        Subscription sub2 = mock(Subscription.class);
        Member member2 = mock(Member.class);

        when(sub2.getId()).thenReturn(2L);
        when(sub2.getMember()).thenReturn(member2);
        when(member2.getName()).thenReturn("신진훈");

        // sub3
        Subscription sub3 = mock(Subscription.class);
        Member member3 = mock(Member.class);

        when(sub3.getId()).thenReturn(3L);
        when(sub3.getMember()).thenReturn(member3);
        when(member3.getName()).thenReturn("김태연");

        FamilySubscription fs2 = mock(FamilySubscription.class);
        FamilySubscription fs3 = mock(FamilySubscription.class);

        when(fs2.getSubscription()).thenReturn(sub2);
        when(fs3.getSubscription()).thenReturn(sub3);

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(fs2, fs3));

        when(subscriptionRepository.findDataPeriodsBySubIds(any()))
                .thenReturn(Map.of(
                        2L, DataPeriod.MONTH,
                        3L, DataPeriod.DAY
                ));

        when(presentDataRepository.findSubUsage(any()))
                .thenReturn(Map.of(
                        1L, new SubUsage(0, 24),
                        2L, new SubUsage(0, -1),
                        3L, new SubUsage(3, 24)
                ));

        FamilyDataResponse expected =
                new FamilyDataResponse(
                        1L,
                        24.0,
                        List.of()
                );

        when(familyDataMapper.toFamilyDataResponse(
                eq(1L),
                any(),
                any(),
                any()
        )).thenReturn(expected);

        FamilyDataResponse result =
                service.findFamilyData(memberId, familyId);

        assertThat(result).isEqualTo(expected);
    }
}
