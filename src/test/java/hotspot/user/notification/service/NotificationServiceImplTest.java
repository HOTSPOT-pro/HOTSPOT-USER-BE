package hotspot.user.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import hotspot.user.member.domain.Member;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.service.port.NotificationRepository;
import hotspot.user.plan.domain.Plan;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("알림 목록 조회 성공: 인증 사용자 회선(subId)으로만 조회한다")
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
                .content("80% 사용")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 0))
                .build();

        given(notificationRepository.findRecentBySubId(eq(mySubId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1));

        NotificationListResponse response = notificationService.findNotifications(memberId, PageRequest.of(0, 10));

        assertThat(response.notifications()).hasSize(1);
        assertThat(response.notifications().get(0).id()).isEqualTo(1L);
        assertThat(response.notifications().get(0).eventId()).isEqualTo("evt-1");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        then(notificationRepository).should().findRecentBySubId(eq(mySubId), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().toString()).contains("createdTime: DESC");
    }

    @Test
    @DisplayName("타 사용자 접근 차단: memberId로 자신의 subId를 찾아 unreadCount를 조회한다")
    void findUnreadCountUsesOnlyOwnSubscription() {
        Long memberId = 10L;
        Long mySubId = 100L;

        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(mySubId).build());
        given(notificationRepository.countUnreadBySubId(mySubId)).willReturn(7L);

        UnreadNotificationCountResponse response = notificationService.findUnreadCount(memberId);

        assertThat(response.unreadCount()).isEqualTo(7L);
        then(notificationRepository).should().countUnreadBySubId(mySubId);
    }

    @Test
    @DisplayName("전체 읽음 처리 성공")
    void markAllReadSuccess() {
        Long memberId = 11L;
        Long mySubId = 200L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(mySubId).build());
        given(notificationRepository.markAllReadBySubId(mySubId)).willReturn(4);

        notificationService.markAllRead(memberId);
        then(notificationRepository).should().markAllReadBySubId(mySubId);
    }
}
