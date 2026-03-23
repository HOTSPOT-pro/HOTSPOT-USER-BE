package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.BlockedTimeResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;

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
    @DisplayName("ONCE 정책이 포함된 경우의 차단 시간대를 조회한다 (startTime 없음)")
    void findMemberBlockedTimeWithOncePolicyNoStartTime() {
        // given
        Long memberId = 1L;
        LocalDateTime modifiedTime = LocalDateTime.of(2026, 3, 23, 10, 0, 0); // 월요일 10시

        BlockPolicyResponse oncePolicy = BlockPolicyResponse.builder()
                .id(100L)
                .policyType(PolicyType.ONCE)
                .isActive(true)
                .modifiedTime(modifiedTime)
                .policySnapshot(PolicySnapshot.builder()
                        .durationMinutes(60)
                        .build())
                .build();

        AppliedPolicyResponse policyResponse = AppliedPolicyResponse.builder()
                .memberId(memberId)
                .subId(10L)
                .blockPolicyResponseList(List.of(oncePolicy))
                .build();

        given(findMemberAppliedPolicyService.findByMemberId(memberId))
                .willReturn(policyResponse);

        // when
        BlockedTimeResponse response = findBlockedTimeService.findMemberBlockedTime(memberId);

        // then
        assertThat(response.dayBlockedTimes()).isNotEmpty();
        // 월요일(MONDAY)에 10:00:00 ~ 11:00:00 범위가 생성되었는지 확인
        assertThat(response.dayBlockedTimes().stream()
                .filter(d -> d.day() == DayOfWeek.MONDAY)
                .flatMap(d -> d.ranges().stream())
                .anyMatch(r -> r.startTime().equals(
                        LocalTime.of(10, 0, 0)) && r.endTime().equals(LocalTime.of(11, 0, 0))))
                .isTrue();
    }

    @Test
    @DisplayName("ONCE 정책이 포함된 경우의 차단 시간대를 조회한다 (startTime 있음)")
    void findMemberBlockedTimeWithOncePolicyWithStartTime() {
        // given
        Long memberId = 1L;
        LocalDateTime modifiedTime = LocalDateTime.of(2026, 3, 23, 10, 0, 0); // 월요일

        BlockPolicyResponse oncePolicy = BlockPolicyResponse.builder()
                .id(101L)
                .policyType(PolicyType.ONCE)
                .isActive(true)
                .modifiedTime(modifiedTime)
                .policySnapshot(PolicySnapshot.builder()
                        .startTime("12:00")
                        .endTime("13:00")
                        .build())
                .build();

        AppliedPolicyResponse policyResponse = AppliedPolicyResponse.builder()
                .memberId(memberId)
                .subId(10L)
                .blockPolicyResponseList(List.of(oncePolicy))
                .build();

        given(findMemberAppliedPolicyService.findByMemberId(memberId))
                .willReturn(policyResponse);

        // when
        BlockedTimeResponse response = findBlockedTimeService.findMemberBlockedTime(memberId);

        // then
        assertThat(response.dayBlockedTimes().stream()
                .filter(d -> d.day() == DayOfWeek.MONDAY)
                .flatMap(d -> d.ranges().stream())
                .anyMatch(r -> r.startTime().equals(
                        LocalTime.of(12, 0, 0)) && r.endTime().equals(LocalTime.of(13, 0, 0))))
                .isTrue();
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
