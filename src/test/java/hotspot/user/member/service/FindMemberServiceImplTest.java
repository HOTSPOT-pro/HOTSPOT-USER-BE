package hotspot.user.member.service;

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
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

/**
 * 회원 조회 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FindMemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @InjectMocks
    private FindMemberServiceImpl findMemberService;

    @Test
    @DisplayName("모든 정보(회원, 소셜, 회선, 가족)가 존재할 때 정상적으로 조회된다")
    void findByIdSuccessAllInfo() {
        // given
        Long memberId = 1L;
        Long subId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .status(Status.APPROVED).build();

        SocialAccount socialAccount = SocialAccount.builder()
                .email("test@email.com")
                .build();

        Subscription subscription = Subscription
                .builder()
                .id(subId)
                .phoneEnc("010-1234-5678")
                .build();

        FamilySubscription familySubscription = FamilySubscription.builder()
                .familyRole(FamilyRole.PARENT)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(socialAccountRepository.findByMemberId(memberId)).willReturn(Optional.of(socialAccount));
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(subscription));
        given(familySubscriptionRepository.findBySubId(subId)).willReturn(Optional.of(familySubscription));

        // when
        MemberResponse response = findMemberService.findById(memberId);

        // then
        assertThat(response.id()).isEqualTo(memberId);
        assertThat(response.email()).isEqualTo("test@email.com");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.familyRole()).isEqualTo(FamilyRole.PARENT);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
    void findByIdFailMemberNotFound() {
        // given
        Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findMemberService.findById(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
