package hotspot.user.notification.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.notification.controller.port.FindNotificationAllowService;
import hotspot.user.notification.controller.port.UpdateNotificationAllowService;
import hotspot.user.notification.controller.request.UpdateNotificationAllowRequest;
import hotspot.user.notification.controller.response.NotificationAllowListResponse;
import hotspot.user.notification.controller.response.NotificationAllowResponse;
import hotspot.user.notification.controller.swagger.NotificationAllowApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications/allow")
public class NotificationAllowController implements NotificationAllowApi {

    private final FindNotificationAllowService findNotificationAllowService;
    private final UpdateNotificationAllowService updateNotificationAllowService;

    // 로그인한 사용자의 알림 허용 설정 목록을 조회한다.
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationAllowListResponse>> getNotificationAllows(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        NotificationAllowListResponse response = findNotificationAllowService.findNotificationAllows(details.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그인한 사용자의 특정 알림 카테고리 허용 상태를 변경한다.
    @Override
    @PatchMapping
    public ResponseEntity<ApiResponse<NotificationAllowResponse>> updateNotificationAllow(
            @AuthenticationPrincipal PrincipalDetails details,
            @Valid @RequestBody UpdateNotificationAllowRequest request
    ) {
        NotificationAllowResponse response = updateNotificationAllowService
                .updateNotificationAllow(details.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
