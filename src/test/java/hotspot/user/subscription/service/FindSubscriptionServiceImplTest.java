package hotspot.user.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import hotspot.user.subscription.controller.response.SubscriptionResponse;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

/**
 * 회선 정보 조회 단위 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class FindSubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private FindSubscriptionServiceImpl findSubscriptionService;

    @Test
    @DisplayName("ID로 회선을 조회하면 SubscriptionResponse를 반환한다")
    void findByIdSuccess() {
        // given
        Long subId = 1L;
        Plan plan = Plan.builder()
                .id(1L)
                .name("베이직")
                .dataAmount(10)
                .dataPeriod(DataPeriod.MONTH)
                .build();

        Member member = Member.builder()
                .id(1L)
                .name("테스트유저")
                .status(Status.APPROVED)
                .build();

        Subscription subscription = Subscription.builder()
                .id(subId)
                .plan(plan)
                .member(member)
                .phoneEnc("010-2345-6789")
                .isLocked(false)
                .build();

        given(subscriptionRepository.findById(subId)).willReturn(Optional.of(subscription));

        // when
        SubscriptionResponse response = findSubscriptionService.findById(subId);

        // then
        assertThat(response.id()).isEqualTo(subId);
        assertThat(response.plan().name()).isEqualTo("베이직");
        assertThat(response.isLocked()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회선 조회 시 예외가 발생한다")
    void findByIdFail() {
        // given
        Long subId = 999L;
        given(subscriptionRepository.findById(subId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findSubscriptionService.findById(subId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 ID로 회선을 조회하면 SubscriptionResponse를 반환한다")
    void findByMemberIdSuccess() {
        // given
        Long memberId = 1L;
        Plan plan = Plan.builder()
                .id(1L)
                .name("베이직")
                .dataAmount(10)
                .dataPeriod(DataPeriod.MONTH).build();

        Member member = Member.builder()
                .id(memberId)
                .name("테스트유저")
                .status(Status.APPROVED)
                .build();

        Subscription subscription = Subscription.builder()
                .id(1L)
                .plan(plan)
                .member(member)
                .phoneEnc("alksjdfkhsjkdfskldfkl")
                .isLocked(false)
                .build();

        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(subscription));

        // when
        SubscriptionResponse response = findSubscriptionService.findByMemberId(memberId);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.plan().name()).isEqualTo("베이직");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 회선 조회 시 예외가 발생한다")
    void findByMemberIdFail() {
        // given
        Long memberId = 999L;
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findSubscriptionService.findByMemberId(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
