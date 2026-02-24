package hotspot.user.auth.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;

@Tag(name = "Auth", description = "인증/인가 관련 API")
public interface AuthApi {

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "재발급 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Refresh Token 쿠키를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.user.common.ApiResponse<TokenResponse>> reissue(
            @Parameter(description = "Refresh Token (Cookie)", in = ParameterIn.COOKIE) String refreshToken);

    @Operation(summary = "온보딩 (최초 가입 승인)", description = "소셜 가입 후 전화번호 인증 등을 거쳐 최종 회원 승인을 완료합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "온보딩 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "회선 정보를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.user.common.ApiResponse<TokenResponse>> onboarding(
            @Valid @RequestBody OnboardingRequest request);

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 로그아웃 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 소유권 검증 실패",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Refresh Token 쿠키를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.user.common.ApiResponse<Void>> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "Refresh Token (Cookie)", in = ParameterIn.COOKIE) String refreshToken);

    @Operation(summary = "회원 탈퇴", description = "사용자의 모든 정보를 삭제(논리 삭제)하고 서비스를 탈퇴합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 소유권 검증 실패",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<hotspot.user.common.ApiResponse<Void>> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "Refresh Token (Cookie)", in = ParameterIn.COOKIE) String refreshToken);
}
