package hotspot.user.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.port.ReissueTokenService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.GlobalErrorCode;
import hotspot.user.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    // [To-Do] 공통 응답 코드 생성 후 응답 수정 하기
    private final ReissueTokenService reissueTokenService;
    private final LogoutService logoutService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ApplicationException(GlobalErrorCode.BAD_REQUEST);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        TokenResponse response = reissueTokenService.reissue(request);

        // 신규 Refresh Token 쿠키 설정
        // [To-Do] 만료 시간 하드 코딩 삭제
        ResponseCookie cookie = CookieUtil.createCookie("refreshToken", response.refreshToken(), 60 * 60 * 24 * 7);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ApplicationException(GlobalErrorCode.BAD_REQUEST);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        logoutService.logout(request);

        ResponseCookie cookie = CookieUtil.deleteCookie("refreshToken");

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
