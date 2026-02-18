package hotspot.user.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;

class SocialAccountTest {

    @Test
    @DisplayName("updateMemberId 메서드 호출 시 연결된 memberId가 변경된 새로운 객체를 반환한다")
    void updateMemberIdSuccess() {
        // given
        SocialAccount socialAccount = SocialAccount.builder()
                .id(10L)
                .email("test@test.com")
                .memberId(1L)
                .build();

        // when
        SocialAccount updatedAccount = socialAccount.updateMemberId(2L);

        // then
        assertThat(updatedAccount.getMemberId()).isEqualTo(2L);
        assertThat(updatedAccount.getEmail()).isEqualTo("test@test.com");
        assertThat(updatedAccount.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("create 정적 팩토리 메서드로 SocialAccount를 생성할 수 있다")
    void createSuccess() {
        // given
        // CreateSocialAccountRequest 순서: name, email, socialId, provider, memberId
        CreateSocialAccountRequest request = new CreateSocialAccountRequest(
                "홍길동", "test@test.com", "social-id", Provider.KAKAO, 1L
        );

        // when
        SocialAccount socialAccount = SocialAccount.create(request);

        // then
        assertThat(socialAccount.getEmail()).isEqualTo("test@test.com");
        assertThat(socialAccount.getMemberId()).isEqualTo(1L);
        assertThat(socialAccount.getProvider()).isEqualTo(Provider.KAKAO);
    }
}
