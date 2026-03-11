package hotspot.user.notification.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.notification.controller.port.FindNotificationService;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.domain.mapper.NotificationMapper;
import hotspot.user.notification.service.port.NotificationRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindNotificationServiceImpl implements FindNotificationService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final NotificationRepository notificationRepository;
    private final SubscriptionService subscriptionService;

    // Pageable을 기본값/정렬로 보정해서 최신 알림을 Slice로 조회하고 목록 응답으로 반환한다.
    @Override
    public NotificationListResponse findNotifications(Long memberId, Pageable pageable) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        Long subId = subscription.getId();
        Pageable normalizedPageable = normalizePageable(pageable);
        Slice<Notification> notifications = notificationRepository.findRecentBySubId(subId, normalizedPageable);
        return NotificationMapper.toListResponse(notifications);
    }

    // 읽지 않은 알림 개수를 조회해 응답으로 반환한다.
    @Override
    public UnreadNotificationCountResponse findUnreadCount(Long memberId) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        Long subId = subscription.getId();
        return UnreadNotificationCountResponse.builder()
                .unreadCount(notificationRepository.countUnreadBySubId(subId))
                .build();
    }

    // pageable이 없거나 값이 부족하면 기본 페이지/사이즈를 적용하고 createdTime 내림차순 정렬이 붙도록 Pageable을 표준화한다.
    private Pageable normalizePageable(Pageable pageable) {
        int page = pageable == null ? 0 : pageable.getPageNumber();
        int size = pageable == null ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdTime")
                        .and(Sort.by(Sort.Direction.DESC, "notificationId"))
        );
    }
}
