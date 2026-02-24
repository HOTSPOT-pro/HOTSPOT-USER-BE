package hotspot.user.kafka.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.kafka.mapper.UserAlertEventNotificationMapper;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.service.port.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAlertEventsConsumer {

    private final UserAlertEventNotificationMapper mapper;
    private final NotificationRepository notificationRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @KafkaListener(
            topics = "${app.topics.user-alert-events}",
            containerFactory = "userAlertKafkaListenerContainerFactory"
    )
    // 이벤트를 수신해 대상별 저장 후 성공 건만 후속 이벤트로 전달한다.
    public void consume(UserAlertEvent event, Acknowledgment acknowledgment) {
        List<Long> targetSubIds = resolveTargetSubIds(event);

        List<Notification> persistedNotifications = new ArrayList<>();
        for (Long targetSubId : targetSubIds) {
            Notification notification = mapper.toNotification(event, targetSubId);
            Notification persistedNotification = notificationRepository.insertIfAbsent(notification);
            if (persistedNotification != null) {
                persistedNotifications.add(persistedNotification);
            }
        }

        if (!persistedNotifications.isEmpty()) {
            applicationEventPublisher.publishEvent(
                    new UserAlertNotificationsPersistedEvent(event, persistedNotifications)
            );
        }

        acknowledgment.acknowledge();
    }

    // subId 우선, 없으면 familyId 기준으로 fan-out 대상 subId 목록을 만든다.
    private List<Long> resolveTargetSubIds(UserAlertEvent event) {
        if (event.subId() != null) {
            return List.of(event.subId());
        }

        Long familyId = event.familyId();
        if (familyId == null) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_REQUIRED);
        }

        return familySubscriptionRepository.findByFamilyId(familyId).stream()
                .map(familySubscription -> familySubscription.getSubscription().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
