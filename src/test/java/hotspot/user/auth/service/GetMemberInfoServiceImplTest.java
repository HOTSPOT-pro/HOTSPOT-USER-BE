package hotspot.user.auth.service;

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

import hotspot.user.auth.controller.response.MemberInfoResponse;
import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class GetMemberInfoServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;
    @Mock
    private PhoneDecryptor phoneDecryptor;

    @InjectMocks
    private GetMemberInfoServiceImpl getMemberInfoService;

    @Test
    @DisplayName("성공: 모든 정보가 존재할 때 회원 상세 정보를 반환한다")
    void getMemberInfoSuccess() {
        // given
        Long memberId = 1L;
        String email = "test@email.com";
        String phoneNumber = "01012345678";

        Member member = Member.builder().id(memberId).name("테스트").status(Status.APPROVED).build();
        SocialAccount socialAccount = SocialAccount.builder().memberId(memberId).email(email).build();
        Subscription subscription = Subscription.builder().id(100L).phoneEnc("enc-phone").build();
        Family family = Family.builder().id(200L).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .family(family)
                .familyRole(FamilyRole.PARENT)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(socialAccountRepository.findByMemberIdAndEmail(memberId, email)).willReturn(Optional.of(socialAccount));
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(subscription));
        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.of(familySub));
        given(phoneDecryptor.decrypt("enc-phone", 100L)).willReturn(phoneNumber);

        // when
        MemberInfoResponse response = getMemberInfoService.getMemberInfo(memberId, email);

        // then
        assertThat(response.name()).isEqualTo("테스트");
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.phone()).isEqualTo(phoneNumber);
        assertThat(response.familyId()).isEqualTo(200L);
        assertThat(response.familyRole()).isEqualTo(FamilyRole.PARENT);
    }

    @Test
    @DisplayName("성공: 가족 결합 정보가 없을 때 familyId는 null, Role은 NONE을 반환한다")
    void getMemberInfoSuccessNoFamily() {
        // given
        Long memberId = 1L;
        String email = "test@email.com";
        String phoneNumber = "01012345678";

        Member member = Member.builder().id(memberId).name("테스트").status(Status.APPROVED).build();
        SocialAccount socialAccount = SocialAccount.builder().memberId(memberId).email(email).build();
        Subscription subscription = Subscription.builder().id(100L).phoneEnc("enc-phone").build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(socialAccountRepository.findByMemberIdAndEmail(memberId, email)).willReturn(Optional.of(socialAccount));
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(subscription));
        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.empty());
        given(phoneDecryptor.decrypt("enc-phone", 100L)).willReturn(phoneNumber);

        // when
        MemberInfoResponse response = getMemberInfoService.getMemberInfo(memberId, email);

        // then
        assertThat(response.familyId()).isNull();
        assertThat(response.familyRole()).isEqualTo(FamilyRole.NONE);
    }

    @Test
    @DisplayName("실패: 회원 정보를 찾을 수 없으면 예외를 발생시킨다")
    void getMemberInfoFailMemberNotFound() {
        // given
        Long memberId = 1L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getMemberInfoService.getMemberInfo(memberId, "test@email.com"))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 회선 정보를 찾을 수 없으면 예외를 발생시킨다")
    void getMemberInfoFailSubscriptionNotFound() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().id(memberId).build();
        SocialAccount socialAccount = SocialAccount.builder().memberId(memberId).build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(socialAccountRepository.findByMemberIdAndEmail(memberId, "test@email.com"))
                .willReturn(Optional.of(socialAccount));
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getMemberInfoService.getMemberInfo(memberId, "test@email.com"))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
