package hotspot.user.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.auth.controller.port.GetMemberInfoService;
import hotspot.user.auth.controller.port.IssueTokenService;
import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.port.OnboardingService;
import hotspot.user.auth.controller.port.ReissueTokenService;
import hotspot.user.auth.controller.port.WithdrawService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.MemberInfoResponse;
import hotspot.user.auth.controller.response.OnboardingResponse;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.auth.controller.swagger.AuthApi;
import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProperties;
import hotspot.user.common.util.cookie.CookieUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {
    private final ReissueTokenService reissueTokenService;
    private final IssueTokenService issueTokenService;
    private final LogoutService logoutService;
    private final OnboardingService onboardingService;
    private final WithdrawService withdrawService;
    private final GetMemberInfoService getMemberInfoService;
    private final JwtProperties jwtProperties;

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ApplicationException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        TokenResponse response = reissueTokenService.reissue(request);

        // 신규 Access Token 쿠키 설정
        ResponseCookie accessCookie = CookieUtil.createCookie("accessToken",
                response.accessToken(),
                jwtProperties.getAccessExpiration());

        // 신규 Refresh Token 쿠키 설정
        ResponseCookie refreshCookie = CookieUtil.createCookie("refreshToken",
                response.refreshToken(),
                jwtProperties.getRefreshExpiration());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardingResponse>> onboarding(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody @Valid OnboardingRequest request) {
        OnboardingResponse response = onboardingService.onboarding(principal.getId(), principal.getEmail(), request);

        // AccessToken 쿠키 설정
        ResponseCookie accessCookie = CookieUtil.createCookie("accessToken",
                response.tokenResponse().accessToken(),
                jwtProperties.getAccessExpiration());

        // Refresh Token 쿠키 설정
        ResponseCookie refreshCookie = CookieUtil.createCookie("refreshToken",
                response.tokenResponse().refreshToken(),
                jwtProperties.getRefreshExpiration());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal PrincipalDetails principal,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new ApplicationException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        logoutService.logout(principal.getId(), request); //  memberId 전달
        
        ResponseCookie accessCookie = CookieUtil.deleteCookie("accessToken");
        ResponseCookie refreshCookie = CookieUtil.deleteCookie("refreshToken");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success());
    }

    @Override
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal PrincipalDetails principal,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new ApplicationException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        TokenRequest request = new TokenRequest(refreshToken);
        withdrawService.withdraw(principal.getId(), request); //  memberId 전달
        
        ResponseCookie accessCookie = CookieUtil.deleteCookie("accessToken");
        ResponseCookie refreshCookie = CookieUtil.deleteCookie("refreshToken");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success());
    }

    // 소셜 로그인 이후 내 정보 조회하는 API
    @Override
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getInfo(
            @AuthenticationPrincipal PrincipalDetails principal) {
        MemberInfoResponse response = getMemberInfoService.getMemberInfo(principal.getId(), principal.getEmail());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
