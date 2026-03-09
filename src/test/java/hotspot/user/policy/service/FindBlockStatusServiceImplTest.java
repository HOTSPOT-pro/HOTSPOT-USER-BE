package hotspot.user.policy.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.policy.controller.response.BlockedStatusResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.subscription.domain.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindBlockStatusServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;
    @Mock
    private PolicySubRepository policySubRepository;
    @Mock
    private BlockPolicyRepository blockPolicyRepository;
    @Mock
    private Clock clock;

    @InjectMocks
    private FindBlockStatusServiceImpl findBlockStatusService;

    private final Long memberId = 1L;
    private final Long subId = 100L;
    private final Long policyId = 10L;

    @BeforeEach
    void setUp() {
    }

    private void setTime(LocalDateTime dateTime) {
        when(clock.instant()).thenReturn(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    @DisplayName("성공: 즉시 차단(isLocked) 상태면 차단된 것으로 간주한다")
    void isLockedReturnsBlocked() {
        // given
        setTime(LocalDateTime.of(2025, 3, 10, 10, 0));
        Subscription sub = Subscription.builder().id(subId).isLocked(true).build();
        FamilySubscription familySub = FamilySubscription.builder().subscription(sub).build();
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(familySub));
        when(policySubRepository.findActiveBySubId(subId)).thenReturn(List.of());

        // when
        BlockedStatusResponse response = findBlockStatusService.findMyBlockStatus(memberId);

        // then
        assertThat(response.isImmediateBlocked()).isTrue();
        assertThat(response.isCurrentlyBlocked()).isTrue();
        assertThat(response.blockedPolicies()).isEmpty();
    }

    @Test
    @DisplayName("성공: SCHEDULED 정책 시간 내에 있으면 차단된다 (주간)")
    void scheduledPolicyBlockingNow() {
        // given
        setTime(LocalDateTime.of(2025, 3, 10, 10, 0));
        Subscription sub = Subscription.builder().id(subId).isLocked(false).build();
        FamilySubscription familySub = FamilySubscription.builder().subscription(sub).build();
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(familySub));

        PolicySub policySub = PolicySub.builder().blockPolicyId(policyId).isActive(true).build();
        when(policySubRepository.findActiveBySubId(subId)).thenReturn(List.of(policySub));

        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("09:00")
                .endTime("18:00")
                .build();
        BlockPolicy policy = BlockPolicy.builder()
                .id(policyId)
                .name("공부 시간")
                .isActive(true)
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(snapshot)
                .build();
        when(blockPolicyRepository.findAllById(List.of(policyId))).thenReturn(List.of(policy));

        // when
        BlockedStatusResponse response = findBlockStatusService.findMyBlockStatus(memberId);

        // then
        assertThat(response.isCurrentlyBlocked()).isTrue();
        assertThat(response.blockedPolicies()).hasSize(1);
        assertThat(response.blockedPolicies().get(0).name()).isEqualTo("공부 시간");
    }

    @Test
    @DisplayName("성공: SCHEDULED 익일 차단 (22:00 ~ 06:00) - 밤 11시에 차단되는지 확인")
    void scheduledOvernightBlockingLateNight() {
        // given: 밤 11시로 시간 설정
        LocalDateTime nightTime = LocalDateTime.of(2025, 3, 10, 23, 0); // 월요일 밤
        setTime(nightTime);

        Subscription sub = Subscription.builder().id(subId).isLocked(false).build();
        FamilySubscription familySub = FamilySubscription.builder().subscription(sub).build();
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(familySub));

        PolicySub policySub = PolicySub.builder().blockPolicyId(policyId).isActive(true).build();
        when(policySubRepository.findActiveBySubId(subId)).thenReturn(List.of(policySub));

        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("22:00")
                .endTime("06:00")
                .build();
        BlockPolicy policy = BlockPolicy.builder()
                .id(policyId)
                .isActive(true)
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(snapshot)
                .build();
        when(blockPolicyRepository.findAllById(List.of(policyId))).thenReturn(List.of(policy));

        // when
        BlockedStatusResponse response = findBlockStatusService.findMyBlockStatus(memberId);

        // then
        assertThat(response.isCurrentlyBlocked()).isTrue();
    }

    @Test
    @DisplayName("성공: SCHEDULED 익일 차단 (22:00 ~ 06:00) - 화요일 새벽 1시에 월요일 차단 정책이 적용되는지 확인")
    void scheduledOvernightBlockingEarlyMorning() {
        // given: 화요일 새벽 1시로 시간 설정
        LocalDateTime morningTime = LocalDateTime.of(2025, 3, 11, 1, 0); // 화요일 새벽
        setTime(morningTime);

        Subscription sub = Subscription.builder().id(subId).isLocked(false).build();
        FamilySubscription familySub = FamilySubscription.builder().subscription(sub).build();
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(familySub));

        PolicySub policySub = PolicySub.builder().blockPolicyId(policyId).isActive(true).build();
        when(policySubRepository.findActiveBySubId(subId)).thenReturn(List.of(policySub));

        // 정책은 월요일 22:00 ~ 06:00 (화요일 새벽까지 차단되어야 함)
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("22:00")
                .endTime("06:00")
                .build();
        BlockPolicy policy = BlockPolicy.builder()
                .id(policyId)
                .isActive(true)
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(snapshot)
                .build();
        when(blockPolicyRepository.findAllById(List.of(policyId))).thenReturn(List.of(policy));

        // when
        BlockedStatusResponse response = findBlockStatusService.findMyBlockStatus(memberId);

        // then
        assertThat(response.isCurrentlyBlocked()).isTrue();
    }

    @Test
    @DisplayName("성공: ONCE 정책은 활성화되어 있다면 현재 차단 중인 것으로 간주한다")
    void oncePolicyBlockingIfActive() {
        // given
        setTime(LocalDateTime.of(2025, 3, 10, 10, 0));
        Subscription sub = Subscription.builder().id(subId).isLocked(false).build();
        FamilySubscription familySub = FamilySubscription.builder().subscription(sub).build();
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(familySub));

        PolicySub policySub = PolicySub.builder().blockPolicyId(policyId).isActive(true).build();
        when(policySubRepository.findActiveBySubId(subId)).thenReturn(List.of(policySub));

        BlockPolicy policy = BlockPolicy.builder()
                .id(policyId)
                .isActive(true)
                .policyType(PolicyType.ONCE)
                .build();
        when(blockPolicyRepository.findAllById(List.of(policyId))).thenReturn(List.of(policy));

        // when
        BlockedStatusResponse response = findBlockStatusService.findMyBlockStatus(memberId);

        // then
        assertThat(response.isCurrentlyBlocked()).isTrue();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 회원 ID 조회 시 예외가 발생한다")
    void memberNotFoundThrowsException() {
        // given
        when(familySubscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findBlockStatusService.findMyBlockStatus(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
