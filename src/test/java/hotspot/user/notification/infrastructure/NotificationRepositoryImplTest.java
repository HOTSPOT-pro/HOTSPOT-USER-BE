package hotspot.user.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    @DisplayName("insertIfAbsent returns true when inserted")
    void insertIfAbsentInserted() {
        Notification notification = Notification.builder()
                .subId(1L)
                .eventId("evt-1")
                .notificationType("ALERT")
                .content("alert")
                .build();

        given(notificationJpaRepository.insertIgnoreConflict(
                eq(1L), eq("evt-1"), eq("ALERT"), eq("alert"), any()))
                .willReturn(1);

        boolean inserted = notificationRepository.insertIfAbsent(notification);

        assertThat(inserted).isTrue();
    }

    @Test
    @DisplayName("insertIfAbsent returns false when duplicated")
    void insertIfAbsentDuplicated() {
        Notification notification = Notification.builder()
                .subId(1L)
                .eventId("evt-1")
                .notificationType("ALERT")
                .content("alert")
                .build();

        given(notificationJpaRepository.insertIgnoreConflict(
                eq(1L), eq("evt-1"), eq("ALERT"), eq("alert"), any()))
                .willReturn(0);

        boolean inserted = notificationRepository.insertIfAbsent(notification);

        assertThat(inserted).isFalse();
    }

    @Test
    @DisplayName("findRecentBySubId returns mapped page")
    void findRecentBySubIdSuccess() {
        NotificationEntity entity = NotificationEntity.builder()
                .notificationId(1L)
                .subscription(SubscriptionEntity.builder().subId(1L).build())
                .eventId("evt-1")
                .notificationType("ALERT")
                .content("alert")
                .isRead(false)
                .build();

        given(notificationJpaRepository.findBySubscriptionSubIdOrderByCreatedTimeDesc(eq(1L), any()))
                .willReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));

        var result = notificationRepository.findRecentBySubId(1L, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventId()).isEqualTo("evt-1");
    }

    @Test
    @DisplayName("count and markAllRead work as expected")
    void countAndMarkAllReadSuccess() {
        given(notificationJpaRepository.countBySubscriptionSubIdAndIsReadFalse(1L)).willReturn(3L);
        given(notificationJpaRepository.markAllReadBySubId(1L)).willReturn(3);

        long count = notificationRepository.countUnreadBySubId(1L);
        int updated = notificationRepository.markAllReadBySubId(1L);

        assertThat(count).isEqualTo(3L);
        assertThat(updated).isEqualTo(3);
        then(notificationJpaRepository).should().countBySubscriptionSubIdAndIsReadFalse(1L);
        then(notificationJpaRepository).should().markAllReadBySubId(1L);
    }
}
