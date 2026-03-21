package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockedTimeResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;

@ExtendWith(MockitoExtension.class)
class FindBlockedTimeServiceImplTest {

    @Mock
    private FindMemberAppliedPolicyService findMemberAppliedPolicyService;

    @Mock
    private FindFamilyAppliedPolicyService findFamilyAppliedPolicyService;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @InjectMocks
    private FindBlockedTimeServiceImpl findBlockedTimeService;

    @Test
    @DisplayName("구성원 ID로 본인의 차단 시간대를 조회한다")
    void findMemberBlockedTimeSuccess() {
        // given
        Long memberId = 1L;
        AppliedPolicyResponse policyResponse = AppliedPolicyResponse.builder()
                .memberId(memberId)
                .subId(10L)
                .blockPolicyResponseList(List.of())
                .build();

        given(findMemberAppliedPolicyService.findByMemberId(memberId))
                .willReturn(policyResponse);

        // when
        BlockedTimeResponse response = findBlockedTimeService.findMemberBlockedTime(memberId);

        // then
        assertThat(response.subId()).isEqualTo(10L);
        assertThat(response.dayBlockedTimes()).hasSize(7); // 일주일치 반환
    }

    @Test
    @DisplayName("성공: OWNER 권한으로 가족 전체의 차단 시간대를 조회한다")
    void findFamilyBlockedTimeSuccessByOwner() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;

        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .family(family)
                .familyRole(FamilyRole.OWNER)
                .build();

        AppliedPolicyResponse memberPolicy = AppliedPolicyResponse.builder()
                .memberId(memberId)
                .subId(10L)
                .blockPolicyResponseList(List.of())
                .build();

        FamilyAppliedPolicyResponse familyPolicyResponse = FamilyAppliedPolicyResponse.builder()
                .familyId(familyId)
                .memberPolicies(List.of(memberPolicy))
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(familySub));

        given(findFamilyAppliedPolicyService.findByMemberId(memberId))
                .willReturn(familyPolicyResponse);

        // when
        List<BlockedTimeResponse> responses = findBlockedTimeService.findFamilyBlockedTime(memberId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).subId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("실패: CHILD 권한으로 가족 전체의 차단 시간대를 조회하려 하면 예외가 발생한다")
    void findFamilyBlockedTimeFailByChild() {
        // given
        Long memberId = 1L;
        FamilySubscription familySub = FamilySubscription.builder()
                .familyRole(FamilyRole.CHILD)
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> findBlockedTimeService.findFamilyBlockedTime(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }
}
