package hotspot.user.notification.controller.swagger;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification", description = "사용자 알림 조회 및 읽음 처리 API")
public interface NotificationApi {

    @Operation(summary = "알림 목록 조회", description = "로그인 사용자의 최근 알림을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회선 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details,
            Pageable pageable
    );

    @Operation(summary = "안 읽은 알림 개수 조회", description = "로그인 사용자의 안 읽은 알림 개수를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회선 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(summary = "알림 전체 읽음 처리", description = "로그인 사용자의 알림을 모두 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회선 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<Void>> markAllRead(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(summary = "알림 단건 읽음 처리", description = "로그인 사용자의 특정 알림 1건을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회선 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<Void>> markRead(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details,
            @Parameter(description = "읽음 처리할 알림 ID", example = "10")
            @PathVariable Long notificationId
    );
}
