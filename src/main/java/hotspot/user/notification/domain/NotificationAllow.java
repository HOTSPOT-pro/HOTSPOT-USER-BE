package hotspot.user.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationAllow {
    private Long id;
    private Long subId;
    private NotificationCategory notificationCategory;
    private Boolean notificationAllow;
    private Boolean isDeleted;

    // 해당 알림 허용 설정을 비활성화 + Soft 삭제 상태의 새 NotificationAllow를 반환한다.
    public NotificationAllow deactivate() {
        return NotificationAllow.builder()
                .id(this.id)
                .subId(this.subId)
                .notificationCategory(this.notificationCategory)
                .notificationAllow(false)
                .isDeleted(true)
                .build();
    }
}
