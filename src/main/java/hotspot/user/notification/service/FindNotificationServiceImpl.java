package hotspot.user.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
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

    @Override
    public NotificationListResponse findNotifications(Long memberId, Pageable pageable) {
        Long subId = resolveSubIdByMemberId(memberId);
        Pageable normalizedPageable = normalizePageable(pageable);
        Page<Notification> notifications = notificationRepository.findRecentBySubId(subId, normalizedPageable);
        return NotificationMapper.toListResponse(notifications);
    }

    @Override
    public UnreadNotificationCountResponse findUnreadCount(Long memberId) {
        Long subId = resolveSubIdByMemberId(memberId);
        return UnreadNotificationCountResponse.builder()
                .unreadCount(notificationRepository.countUnreadBySubId(subId))
                .build();
    }

    private Long resolveSubIdByMemberId(Long memberId) {
        try {
            Subscription subscription = subscriptionService.findByMemberId(memberId);
            return subscription.getId();
        } catch (ApplicationException e) {
            if (e.getCode() == SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND) {
                throw new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
            }
            throw e;
        }
    }

    private Pageable normalizePageable(Pageable pageable) {
        int page = pageable == null ? 0 : pageable.getPageNumber();
        int size = pageable == null ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
    }
}
