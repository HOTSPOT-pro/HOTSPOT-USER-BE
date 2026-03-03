package hotspot.user.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.infrastructure.entity.NotificationEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplTest {

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @InjectMocks
    private NotificationRepositoryImpl notificationRepository;

    @Test
    @DisplayName("insertIfAbsent returns persisted notification when inserted")
    void insertIfAbsentInserted() {
        Notification notification = Notification.builder()
                .subId(1L)
                .eventId("evt-1")
                .notificationType("ALERT")
                .title("title")
                .content("alert")
                .createdTime(LocalDateTime.of(2026, 2, 23, 12, 0))
                .build();
        NotificationEntity persisted = NotificationEntity.builder()
                .notificationId(10L)
                .subscription(SubscriptionEntity.builder().subId(1L).build())
                .eventId("evt-1")
                .notificationType("ALERT")
                .title("title")
                .content("alert")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 12, 0))
                .build();

        given(notificationJpaRepository.insertIgnoreConflict(
                eq(1L), eq("evt-1"), eq("ALERT"), eq("title"), eq("alert"), any()))
                .willReturn(1);
        given(notificationJpaRepository.findByEventIdAndSubscriptionSubId("evt-1", 1L))
                .willReturn(Optional.of(persisted));

        Notification inserted = notificationRepository.insertIfAbsent(notification);

        assertThat(inserted).isNotNull();
        assertThat(inserted.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("insertIfAbsent returns null when duplicated")
    void insertIfAbsentDuplicated() {
        Notification notification = Notification.builder()
                .subId(1L)
                .eventId("evt-1")
                .notificationType("ALERT")
                .title("title")
                .content("alert")
                .build();

        given(notificationJpaRepository.insertIgnoreConflict(
                eq(1L), eq("evt-1"), eq("ALERT"), eq("title"), eq("alert"), any()))
                .willReturn(0);

        Notification inserted = notificationRepository.insertIfAbsent(notification);

        assertThat(inserted).isNull();
    }

    @Test
    @DisplayName("findRecentBySubId returns mapped page")
    void findRecentBySubIdSuccess() {
        NotificationEntity entity = NotificationEntity.builder()
                .notificationId(1L)
                .subscription(SubscriptionEntity.builder().subId(1L).build())
                .eventId("evt-1")
                .notificationType("ALERT")
                .title("title")
                .content("alert")
                .isRead(false)
                .build();

        given(notificationJpaRepository
                .findBySubscriptionSubIdAndCreatedTimeGreaterThanEqual(
                        eq(1L), any(), any()))
                .willReturn(new SliceImpl<>(List.of(entity), PageRequest.of(0, 20), false));

        var result = notificationRepository.findRecentBySubId(1L, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventId()).isEqualTo("evt-1");
    }

    @Test
    @DisplayName("count and markAllRead work as expected")
    void countAndMarkAllReadSuccess() {
        given(notificationJpaRepository
                .countBySubscriptionSubIdAndIsReadFalseAndCreatedTimeGreaterThanEqual(eq(1L), any()))
                .willReturn(3L);
        given(notificationJpaRepository
                .markAllReadBySubIdAndCreatedTimeGreaterThanEqual(eq(1L), any()))
                .willReturn(3);

        long count = notificationRepository.countUnreadBySubId(1L);
        int updated = notificationRepository.markAllReadBySubId(1L);

        assertThat(count).isEqualTo(3L);
        assertThat(updated).isEqualTo(3);
        then(notificationJpaRepository).should()
                .countBySubscriptionSubIdAndIsReadFalseAndCreatedTimeGreaterThanEqual(eq(1L), any());
        then(notificationJpaRepository).should()
                .markAllReadBySubIdAndCreatedTimeGreaterThanEqual(eq(1L), any());
    }

    @Test
    @DisplayName("markReadById delegates to jpa query")
    void markReadByIdSuccess() {
        given(notificationJpaRepository.markReadByIdAndSubId(10L, 1L)).willReturn(1);

        int updated = notificationRepository.markReadById(10L, 1L);

        assertThat(updated).isEqualTo(1);
        then(notificationJpaRepository).should().markReadByIdAndSubId(10L, 1L);
    }
}
