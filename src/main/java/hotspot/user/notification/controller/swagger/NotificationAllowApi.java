package hotspot.user.notification.controller.swagger;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.notification.controller.request.UpdateNotificationAllowRequest;
import hotspot.user.notification.controller.response.NotificationAllowListResponse;
import hotspot.user.notification.controller.response.NotificationAllowResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification Allow", description = "사용자 알림 허용 설정 API")
public interface NotificationAllowApi {

    @Operation(summary = "알림 허용 설정 조회", description = "로그인한 사용자의 카테고리별 알림 허용 여부를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패\n"
                            + "- AUTH_002: 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 대상을 찾을 수 없음\n"
                            + "- SUB_001: 회선 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<NotificationAllowListResponse>> getNotificationAllows(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(summary = "알림 허용 설정 수정", description = "로그인한 사용자의 카테고리별 알림 허용 여부를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청\n"
                            + "- COMMON_002: 입력값 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패\n"
                            + "- AUTH_002: 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "처리 대상을 찾을 수 없음\n"
                            + "- SUB_001: 회선 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<NotificationAllowResponse>> updateNotificationAllow(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details,
            @Valid @RequestBody UpdateNotificationAllowRequest request
    );
}
