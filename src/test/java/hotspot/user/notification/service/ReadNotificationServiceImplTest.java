package hotspot.user.notification.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.notification.service.port.NotificationRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class ReadNotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private ReadNotificationServiceImpl readNotificationService;

    @Test
    @DisplayName("mark all read success")
    void markAllReadSuccess() {
        Long memberId = 11L;
        Long mySubId = 200L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(mySubId).build());
        given(notificationRepository.markAllReadBySubId(mySubId)).willReturn(4);

        readNotificationService.markAllRead(memberId);
        then(notificationRepository).should().markAllReadBySubId(mySubId);
    }

    @Test
    @DisplayName("mark single read success")
    void markReadSuccess() {
        Long memberId = 11L;
        Long mySubId = 200L;
        Long notificationId = 33L;

        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(mySubId).build());
        given(notificationRepository.markReadById(notificationId, mySubId)).willReturn(1);

        readNotificationService.markRead(memberId, notificationId);

        then(notificationRepository).should().markReadById(notificationId, mySubId);
    }

    @Test
    @DisplayName("mark single read fail when notification not found")
    void markReadFailWhenNotificationNotFound() {
        Long memberId = 11L;
        Long mySubId = 200L;
        Long notificationId = 33L;

        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(mySubId).build());
        given(notificationRepository.markReadById(notificationId, mySubId)).willReturn(0);

        assertThatThrownBy(() -> readNotificationService.markRead(memberId, notificationId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(NotificationErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("mark all read fail when subscription not found")
    void markAllReadFailWhenSubscriptionNotFound() {
        Long memberId = 11L;
        given(subscriptionService.findByMemberId(memberId))
                .willThrow(new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        assertThatThrownBy(() -> readNotificationService.markAllRead(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
