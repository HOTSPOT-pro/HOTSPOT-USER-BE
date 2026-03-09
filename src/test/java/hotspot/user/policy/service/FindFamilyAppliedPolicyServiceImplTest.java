package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.PriorityType;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.Member;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.infrastructure.schema.FamilyDataControl;
import hotspot.user.policy.service.port.FamilyDataLimitRepository;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class FindFamilyAppliedPolicyServiceImplTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private FindMemberAppliedPolicyService findMemberAppliedPolicyService;

    @Mock
    private FamilyDataLimitRepository familyDataLimitRepository;

    @InjectMocks
    private FindFamilyAppliedPolicyServiceImpl findFamilyAppliedPolicyService;

    @Test
    @DisplayName("가족 ID로 전체 구성원의 통합 정책을 조회한다")
    void findFamilyAppliedPoliciesSuccess() {

        // given
        Long familyId = 1L;
        Long memberId = 10L;
        Long subId = 100L;

        Family family = Family.builder()
                .id(familyId)
                .familyNum(2)
                .familyDataAmount(100)
                .priorityType(PriorityType.FIFO)
                .build();

        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .build();

        Subscription sub = Subscription.builder()
                .id(subId)
                .member(member)
                .build();

        FamilySubscription mapping = FamilySubscription.builder()
                .subscription(sub)
                .build();

        AppliedPolicyResponse memberResponse =
                AppliedPolicyResponse.builder()
                        .memberId(memberId)
                        .memberName("홍길동")
                        .subId(subId)
                        .build();

        FamilyDataControl redisData =
                new FamilyDataControl(
                        1024L * 1024L, // familyDataLimit
                        List.of(
                                new FamilyDataControl.SubFamilyDataControl(
                                        subId,
                                        1024L * 100,   // familyDataUsage
                                        1024L * 512    // familyDataSubLimit
                                )
                        )
                );

        given(familyRepository.findById(familyId))
                .willReturn(Optional.of(family));

        given(familySubscriptionRepository.findByFamilyId(familyId))
                .willReturn(List.of(mapping));

        given(findMemberAppliedPolicyService.findByMemberId(memberId))
                .willReturn(memberResponse);

        given(familyDataLimitRepository.findFamilyDataLimit(familyId))
                .willReturn(redisData);

        // when
        FamilyAppliedPolicyResponse response =
                findFamilyAppliedPolicyService.findByFamilyId(familyId);

        // then
        assertThat(response.familyId()).isEqualTo(familyId);
        assertThat(response.memberPolicies()).hasSize(1);
        assertThat(response.memberPolicies().get(0).memberName()).isEqualTo("홍길동");
    }
}
