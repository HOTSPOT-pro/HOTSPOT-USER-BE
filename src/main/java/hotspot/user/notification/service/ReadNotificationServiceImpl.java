package hotspot.user.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.notification.controller.port.ReadNotificationService;
import hotspot.user.notification.service.port.NotificationRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadNotificationServiceImpl implements ReadNotificationService {

    private final NotificationRepository notificationRepository;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void markAllRead(Long memberId) {
        Long subId = resolveSubIdByMemberId(memberId);
        notificationRepository.markAllReadBySubId(subId);
    }

    @Override
    @Transactional
    public void markRead(Long memberId, Long notificationId) {
        Long subId = resolveSubIdByMemberId(memberId);
        int updatedCount = notificationRepository.markReadById(notificationId, subId);
        if (updatedCount == 0) {
            throw new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
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
}
