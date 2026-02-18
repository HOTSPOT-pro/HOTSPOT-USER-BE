package hotspot.user.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import hotspot.user.auth.controller.port.SaveTokenService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;

/**
 * 토큰 발행 Service 단위 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class IssueTokenServiceImplTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private SaveTokenService saveTokenService;

    @InjectMocks
    private IssueTokenServiceImpl issueTokenService;

    @Test
    @DisplayName("토큰 발급 성공: 인증 객체를 생성하고 토큰을 발행 및 저장한다")
    void issueSuccess() {
        // given
        Member member = Member.builder().id(1L).status(Status.APPROVED).build();
        String email = "test@test.com";
        FamilyRole role = FamilyRole.CHILD;

        given(jwtProvider.createAccessToken(any(Authentication.class))).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any(Authentication.class))).willReturn("refresh-token");

        // when
        TokenResponse response = issueTokenService.issue(member, email, role);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(saveTokenService).saveToken(eq(1L), any(TokenRequest.class));
    }
}
