package hotspot.user.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.notification.controller.port.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final NotificationRepository notificationRepository;
    private final SubscriptionService subscriptionService;

    // memberId -> subId를 해석한 뒤 최근 알림을 조회한다.
    @Override
    public NotificationListResponse findNotifications(Long memberId, Pageable pageable) {
        Long subId = resolveSubIdByMemberId(memberId);
        Pageable normalizedPageable = normalizePageable(pageable);
        Page<Notification> notifications = notificationRepository.findRecentBySubId(subId, normalizedPageable);
        return NotificationMapper.toListResponse(notifications);
    }

    // memberId -> subId를 해석한 뒤 안 읽은 개수를 조회한다.
    @Override
    public UnreadNotificationCountResponse findUnreadCount(Long memberId) {
        Long subId = resolveSubIdByMemberId(memberId);
        return new UnreadNotificationCountResponse(notificationRepository.countUnreadBySubId(subId));
    }

    // memberId -> subId를 해석한 뒤 알림 전체를 읽음 처리한다.
    @Override
    @Transactional
    public void markAllRead(Long memberId) {
        Long subId = resolveSubIdByMemberId(memberId);
        notificationRepository.markAllReadBySubId(subId);
    }

    // 인증 사용자(memberId)의 소유 회선(subId)을 조회한다.
    private Long resolveSubIdByMemberId(Long memberId) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        return subscription.getId();
    }

    // 기본 정렬(createdTime DESC)과 기본 페이지 크기를 강제한다.
    private Pageable normalizePageable(Pageable pageable) {
        int page = pageable == null ? 0 : pageable.getPageNumber();
        int size = pageable == null ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
    }
}
