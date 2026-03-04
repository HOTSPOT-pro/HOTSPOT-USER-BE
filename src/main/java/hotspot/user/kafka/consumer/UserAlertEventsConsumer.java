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
import hotspot.user.common.exception.code.UserAlertConsumerErrorCode;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.kafka.mapper.orchestrator.UserAlertEventNotificationMapper;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import hotspot.user.notification.service.port.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserAlertEventsConsumer {

    private final UserAlertEventNotificationMapper mapper;
    private final NotificationRepository notificationRepository;
    private final NotificationAllowRepository notificationAllowRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // Kafka에서 유저 알림 이벤트를 받아 대상 구독자별로 알림을 생성하고(타입/허용 여부 검증 포함) 중복 없이 저장한 뒤 저장된 게 있으면 후속 이벤트 발행 후 ACK 한다.
    @Transactional
    @KafkaListener(
            topics = "${app.topics.user-alert-events}",
            containerFactory = "userAlertKafkaListenerContainerFactory"
    )
    public void consume(UserAlertEvent event, Acknowledgment acknowledgment) {
        List<Long> targetSubIds;
        try {
            targetSubIds = resolveTargetSubIds(event);
        } catch (Exception ex) {
            logSkip("target", event, event.subId(), ex, UserAlertConsumerErrorCode.TARGET_RESOLUTION_FAILED);
            acknowledgment.acknowledge();
            return;
        }

        List<Notification> persistedNotifications = new ArrayList<>();
        for (Long targetSubId : targetSubIds) {
            Notification notification;
            try {
                notification = mapper.toNotification(event, targetSubId);
            } catch (Exception ex) {
                logSkip("mapping", event, targetSubId, ex, UserAlertConsumerErrorCode.NOTIFICATION_MAPPING_FAILED);
                continue;
            }
            NotificationCategory category;

            try {
                NotificationType notificationType = NotificationType.from(notification.getNotificationType());
                category = NotificationType.isAlwaysAllowed(notificationType)
                        ? null
                        : notificationType.category();
            } catch (Exception ex) {
                logSkip("type", event, targetSubId, ex, UserAlertConsumerErrorCode.NOTIFICATION_TYPE_RESOLUTION_FAILED);
                continue;
            }

            if (category != null && !isNotificationAllowed(targetSubId, category)) {
                continue;
            }

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

    // 이벤트에 subId가 있으면 단건 대상으로, 없으면 familyId로 가족 구성원의 subId 목록을 조회해 알림 대상들을 결정한다.
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

    // 해당 subId가 해당 알림 카테고리를 수신 허용했는지 설정값을 조회해 true/false를 반환한다.
    private boolean isNotificationAllowed(Long subId, NotificationCategory category) {
        return notificationAllowRepository.findBySubIdAndCategory(subId, category)
                .map(notificationAllow -> Boolean.TRUE.equals(notificationAllow.getNotificationAllow()))
                .orElse(false);
    }

    private String resolveErrorCode(Exception ex, UserAlertConsumerErrorCode fallbackCode) {
        if (ex instanceof ApplicationException appEx) {
            return appEx.getCode().getCustomCode();
        }
        return fallbackCode.getCustomCode();
    }

    private void logSkip(
            String stage,
            UserAlertEvent event,
            Long subId,
            Exception ex,
            UserAlertConsumerErrorCode fallbackCode
    ) {
        log.warn(
                "Skip user alert consume. stage={}, eventId={}, subId={}, familyId={}, errorCode={}, reason={}",
                stage,
                event.alertId(),
                subId,
                event.familyId(),
                resolveErrorCode(ex, fallbackCode),
                ex.getMessage()
        );
    }
}
