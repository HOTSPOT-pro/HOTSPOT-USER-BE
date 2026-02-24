package hotspot.user.notification.service.port;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import hotspot.user.notification.domain.Notification;

public interface NotificationRepository {

    // (eventId, subId)가 없을 때만 저장하고, 중복이면 저장하지 않는다.
    Notification insertIfAbsent(Notification notification);

    // 특정 회선의 최근 알림을 페이지 단위로 조회한다.
    Slice<Notification> findRecentBySubId(Long subId, Pageable pageable);

    // 특정 회선의 안 읽은 알림 개수를 조회한다.
    long countUnreadBySubId(Long subId);

    // 특정 회선의 알림을 전체 읽음 처리한다.
    int markAllReadBySubId(Long subId);

    // 특정 회선 소유 알림 1건을 읽음 처리한다.
    int markReadById(Long notificationId, Long subId);
}
