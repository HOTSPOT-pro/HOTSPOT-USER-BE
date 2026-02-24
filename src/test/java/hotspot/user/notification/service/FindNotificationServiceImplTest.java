package hotspot.user.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.member.domain.Member;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.service.port.NotificationRepository;
import hotspot.user.plan.domain.Plan;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class FindNotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private FindNotificationServiceImpl findNotificationService;

    @Test
    @DisplayName("find notifications success")
    void findNotificationsSuccess() {
        Long memberId = 10L;
        Long mySubId = 100L;

        Subscription mySubscription = Subscription.builder()
                .id(mySubId)
                .member(Member.builder().id(memberId).build())
                .plan(Plan.builder().id(1L).build())
                .isLocked(false)
                .build();
        given(subscriptionService.findByMemberId(memberId)).willReturn(mySubscription);

        Notification notification = Notification.builder()
                .id(1L)
                .subId(mySubId)
                .eventId("evt-1")
                .notificationType("ALERT")
                .content("80% used")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 0))
                .build();

        given(notificationRepository.findRecentBySubId(eq(mySubId), any(Pageable.class)))
                .willReturn(new SliceImpl<>(List.of(notification), PageRequest.of(0, 20), false));

        NotificationListResponse response = findNotificationService.findNotifications(memberId, PageRequest.of(0, 10));
        @SuppressWarnings("unchecked")
        List<Object> notifications = (List<Object>) ReflectionTestUtils.getField(response, "notifications");
        Object first = notifications.get(0);

        assertThat(notifications).hasSize(1);
        assertThat(ReflectionTestUtils.getField(first, "id")).isEqualTo(1L);
        assertThat(ReflectionTestUtils.getField(first, "eventId")).isEqualTo("evt-1");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(notificationRepository).should().findRecentBySubId(eq(mySubId), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().toString()).contains("createdTime: DESC");
    }

    @Test
    @DisplayName("find unread count success")
    void findUnreadCountUsesOnlyOwnSubscription() {
        Long memberId = 10L;
        Long mySubId = 100L;

        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(mySubId).build());
        given(notificationRepository.countUnreadBySubId(mySubId)).willReturn(7L);

        UnreadNotificationCountResponse response = findNotificationService.findUnreadCount(memberId);

        assertThat(ReflectionTestUtils.getField(response, "unreadCount")).isEqualTo(7L);
        then(notificationRepository).should().countUnreadBySubId(mySubId);
    }

    @Test
    @DisplayName("find unread count fail when subscription not found")
    void findUnreadCountFailWhenSubscriptionNotFound() {
        Long memberId = 10L;
        given(subscriptionService.findByMemberId(memberId))
                .willThrow(new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        assertThatThrownBy(() -> findNotificationService.findUnreadCount(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
