package hotspot.user.notification.domain.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hotspot.user.notification.controller.response.NotificationAllowListResponse;
import hotspot.user.notification.controller.response.NotificationAllowResponse;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;

public final class NotificationAllowMapper {

    private NotificationAllowMapper() {
    }

    // NotificationAllow 1건을 API 응답용 DTO로 변환한다.
    public static NotificationAllowResponse toResponse(NotificationAllow notificationAllow) {
        return NotificationAllowResponse.builder()
                .notificationCategory(notificationAllow.getNotificationCategory())
                .notificationAllow(notificationAllow.getNotificationAllow())
                .build();
    }

    // DB에 있는 알림 허용 목록을 카테고리별 맵으로 만든 뒤, 모든 카테고리를 포함해 목록 응답으로 변환한다.
    public static NotificationAllowListResponse toListResponse(List<NotificationAllow> allows) {
        Map<NotificationCategory, Boolean> allowByCategory = allows.stream()
                .collect(Collectors.toMap(
                        NotificationAllow::getNotificationCategory,
                        NotificationAllow::getNotificationAllow,
                        (left, right) -> right
                ));

        List<NotificationAllowResponse> responses = List.of(NotificationCategory.values()).stream()
                .map(category -> NotificationAllowResponse.builder()
                        .notificationCategory(category)
                        .notificationAllow(allowByCategory.getOrDefault(category, false))
                        .build())
                .toList();

        return NotificationAllowListResponse.builder()
                .notificationAllows(responses)
                .build();
    }
}
