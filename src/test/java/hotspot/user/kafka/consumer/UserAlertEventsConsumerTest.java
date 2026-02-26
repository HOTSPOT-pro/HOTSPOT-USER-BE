package hotspot.user.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.support.Acknowledgment;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.kafka.mapper.orchestrator.UserAlertEventNotificationMapper;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import hotspot.user.notification.service.port.NotificationRepository;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class UserAlertEventsConsumerTest {

    @Mock
    private UserAlertEventNotificationMapper mapper;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationAllowRepository notificationAllowRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private UserAlertEventsConsumer consumer;

    @BeforeEach
    void setUp() {
        lenient().when(notificationAllowRepository.findBySubIdAndCategory(anyLong(), any(NotificationCategory.class)))
                .thenReturn(Optional.of(NotificationAllow.builder()
                        .notificationAllow(true)
                        .build()));
    }

    @Test
    @DisplayName("subId target: persist and publish only inserted notifications, then ack")
    // subId 대상일 때 저장/발행/ack 흐름을 검증한다.
    void consumeWithSubIdTarget() {
        UserAlertEvent event = event(101L, null, "evt-sub");
        Notification notification = notification(101L, "evt-sub");
        Notification persisted = persistedNotification(1L, notification);
        given(mapper.toNotification(event, 101L)).willReturn(notification);
        given(notificationRepository.insertIfAbsent(notification)).willReturn(persisted);

        consumer.consume(event, acknowledgment);

        then(mapper).should().toNotification(event, 101L);
        then(notificationRepository).should().insertIfAbsent(notification);

        ArgumentCaptor<UserAlertNotificationsPersistedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserAlertNotificationsPersistedEvent.class);
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().sourceEvent()).isEqualTo(event);
        assertThat(eventCaptor.getValue().persistedNotifications()).containsExactly(persisted);

        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("does not persist or publish when category is not allowed")
    // category 허용 설정이 없거나 false면 저장/발행 없이 ack만 수행하는지 검증한다.
    void consumeSkipsWhenNotificationCategoryDisallowed() {
        UserAlertEvent event = event(101L, null, "evt-disallowed");
        Notification notification = notification(101L, "evt-disallowed");
        given(mapper.toNotification(event, 101L)).willReturn(notification);
        given(notificationAllowRepository.findBySubIdAndCategory(101L, NotificationCategory.DATA))
                .willReturn(Optional.empty());

        consumer.consume(event, acknowledgment);

        then(notificationRepository).shouldHaveNoInteractions();
        then(applicationEventPublisher).shouldHaveNoInteractions();
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("familyId target: fan-out and publish only successfully inserted notifications")
    // familyId 대상일 때 fan-out 저장과 성공 건 발행을 검증한다.
    void consumeWithFamilyFanOut() {
        UserAlertEvent event = event(null, 200L, "evt-family");
        List<FamilySubscription> familySubscriptions = List.of(
                familySubscription(11L, 200L),
                familySubscription(22L, 200L)
        );
        given(familySubscriptionRepository.findByFamilyId(200L)).willReturn(familySubscriptions);

        Notification first = notification(11L, "evt-family");
        Notification second = notification(22L, "evt-family");
        Notification persistedFirst = persistedNotification(10L, first);
        given(mapper.toNotification(event, 11L)).willReturn(first);
        given(mapper.toNotification(event, 22L)).willReturn(second);
        given(notificationRepository.insertIfAbsent(first)).willReturn(persistedFirst);
        given(notificationRepository.insertIfAbsent(second)).willReturn(null);

        consumer.consume(event, acknowledgment);

        then(familySubscriptionRepository).should().findByFamilyId(200L);
        then(mapper).should().toNotification(event, 11L);
        then(mapper).should().toNotification(event, 22L);
        then(notificationRepository).should().insertIfAbsent(first);
        then(notificationRepository).should().insertIfAbsent(second);

        ArgumentCaptor<UserAlertNotificationsPersistedEvent> eventCaptor =
                ArgumentCaptor.forClass(UserAlertNotificationsPersistedEvent.class);
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().persistedNotifications()).containsExactly(persistedFirst);

        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("does not publish follow-up event when every insert was skipped")
    // 저장 성공이 없으면 후속 이벤트를 발행하지 않는지 검증한다.
    void consumeWithoutInsertedRows() {
        UserAlertEvent event = event(303L, null, "evt-dup");
        Notification notification = notification(303L, "evt-dup");
        given(mapper.toNotification(event, 303L)).willReturn(notification);
        given(notificationRepository.insertIfAbsent(notification)).willReturn(null);

        consumer.consume(event, acknowledgment);

        then(applicationEventPublisher).shouldHaveNoInteractions();
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("throws when neither subId nor familyId exists and does not ack")
    // 대상 정보가 없으면 예외가 나고 ack가 호출되지 않는지 검증한다.
    void consumeWithoutTarget() {
        UserAlertEvent event = event(null, null, "evt-invalid");

        assertThatThrownBy(() -> consumer.consume(event, acknowledgment))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.KAFKA_SUB_ID_REQUIRED.getMessage());

        then(acknowledgment).shouldHaveNoInteractions();
        then(applicationEventPublisher).shouldHaveNoInteractions();
        then(notificationRepository).shouldHaveNoInteractions();
        then(mapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("acks and skips when notification type has no category mapping")
    void consumeWithUnknownNotificationType() {
        UserAlertEvent event = event(101L, null, "evt-unknown-type");
        Notification unknownTypeNotification = Notification.builder()
                .subId(101L)
                .eventId("evt-unknown-type")
                .notificationType("UNKNOWN_TYPE")
                .title("unknown")
                .content("unknown")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();
        given(mapper.toNotification(event, 101L)).willReturn(unknownTypeNotification);

        consumer.consume(event, acknowledgment);

        then(notificationRepository).shouldHaveNoInteractions();
        then(applicationEventPublisher).shouldHaveNoInteractions();
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("family apply result notifications are persisted even without allow setting")
    void consumeFamilyApplyResultAlwaysAllowed() {
        UserAlertEvent event = event(101L, null, "evt-family-apply");
        Notification notification = Notification.builder()
                .subId(101L)
                .eventId("evt-family-apply")
                .notificationType("FAMILY_MEMBER_ADD_APPROVED")
                .title("승인")
                .content("추가 승인")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();
        Notification persisted = persistedNotification(33L, notification);
        given(mapper.toNotification(event, 101L)).willReturn(notification);
        given(notificationRepository.insertIfAbsent(notification)).willReturn(persisted);

        consumer.consume(event, acknowledgment);

        then(notificationAllowRepository).shouldHaveNoInteractions();
        then(notificationRepository).should().insertIfAbsent(notification);
        then(applicationEventPublisher).should().publishEvent(any(UserAlertNotificationsPersistedEvent.class));
        then(acknowledgment).should().acknowledge();
    }

    // 테스트용 이벤트 객체를 생성한다.
    private UserAlertEvent event(Long subId, Long familyId, String alertId) {
        return new UserAlertEvent(
                alertId,
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                subId,
                familyId,
                "30",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }

    // 테스트용 Notification 객체를 생성한다.
    private Notification notification(Long subId, String eventId) {
        return Notification.builder()
                .subId(subId)
                .eventId(eventId)
                .notificationType("SINGLE_USAGE_THRESHOLD_30")
                .title("Data alert")
                .content("test")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();
    }

    private Notification persistedNotification(Long id, Notification notification) {
        return Notification.builder()
                .id(id)
                .subId(notification.getSubId())
                .eventId(notification.getEventId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdTime(notification.getCreatedTime())
                .build();
    }

    // 테스트용 FamilySubscription 객체를 생성한다.
    private FamilySubscription familySubscription(Long subId, Long familyId) {
        return FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .subscription(Subscription.builder().id(subId).build())
                .build();
    }
}
