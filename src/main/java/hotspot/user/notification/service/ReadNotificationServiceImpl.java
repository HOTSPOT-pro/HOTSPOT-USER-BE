package hotspot.user.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
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

    // 해당 subId의 모든 알림을 "읽음 처리"로 일괄 업데이트한다.
    @Override
    @Transactional
    public void markAllRead(Long memberId) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        Long subId = subscription.getId();
        notificationRepository.markAllReadBySubId(subId);
    }

    // 해당 subId의 특정 알림 1건을 "읽음 처리"로 업데이트한다.
    @Override
    @Transactional
    public void markRead(Long memberId, Long notificationId) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        Long subId = subscription.getId();
        int updatedCount = notificationRepository.markReadById(notificationId, subId);
        if (updatedCount == 0) {
            throw new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }
}
