package hotspot.user.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.notification.controller.port.NotificationService;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;
import hotspot.user.notification.controller.swagger.NotificationApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    // 로그인 사용자의 최근 알림 목록을 페이지 단위로 조회한다.
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal PrincipalDetails details,
            Pageable pageable
    ) {
        NotificationListResponse response = notificationService.findNotifications(details.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그인 사용자의 안 읽은 알림 개수를 조회한다.
    @Override
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> getUnreadCount(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        UnreadNotificationCountResponse response = notificationService.findUnreadCount(details.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그인 사용자의 알림을 모두 읽음 상태로 변경한다.
    @Override
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        notificationService.markAllRead(details.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 로그인 사용자의 특정 알림 1건을 읽음 상태로 변경한다.
    @Override
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal PrincipalDetails details,
            @PathVariable Long notificationId
    ) {
        notificationService.markRead(details.getId(), notificationId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
