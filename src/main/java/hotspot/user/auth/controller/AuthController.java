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
import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.jwt.JwtProperties;
import hotspot.user.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final ReissueTokenService reissueTokenService;
    private final LogoutService logoutService;
    private final JwtProperties jwtProperties;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ApplicationException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        TokenResponse response = reissueTokenService.reissue(request);

        // 신규 Refresh Token 쿠키 설정
        ResponseCookie cookie = CookieUtil.createCookie("refreshToken",
                response.refreshToken(),
                jwtProperties.getRefreshExpiration());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new ApplicationException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        logoutService.logout(request);
        ResponseCookie cookie = CookieUtil.deleteCookie("refreshToken");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success());
    }
}
