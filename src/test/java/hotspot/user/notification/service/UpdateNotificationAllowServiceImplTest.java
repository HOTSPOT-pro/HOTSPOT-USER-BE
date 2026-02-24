package hotspot.user.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.notification.controller.request.UpdateNotificationAllowRequest;
import hotspot.user.notification.controller.response.NotificationAllowResponse;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationAllowServiceImplTest {

    @Mock
    private NotificationAllowRepository notificationAllowRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private UpdateNotificationAllowServiceImpl updateNotificationAllowService;

    @Test
    @DisplayName("allow request returns active record when already allowed")
    void allowWhenAlreadyAllowedReturnsExisting() {
        Long memberId = 10L;
        Long subId = 100L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(subId).build());
        given(notificationAllowRepository.findBySubIdAndCategory(subId, NotificationCategory.POLICY))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .id(7L)
                        .subId(subId)
                        .notificationCategory(NotificationCategory.POLICY)
                        .notificationAllow(true)
                        .isDeleted(false)
                        .build()));

        NotificationAllowResponse response = updateNotificationAllowService.updateNotificationAllow(
                memberId,
                new UpdateNotificationAllowRequest(NotificationCategory.POLICY, true)
        );

        assertThat(ReflectionTestUtils.getField(response, "notificationCategory"))
                .isEqualTo(NotificationCategory.POLICY);
        assertThat(ReflectionTestUtils.getField(response, "notificationAllow"))
                .isEqualTo(true);
        then(notificationAllowRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("allow request inserts new history when no active record")
    void allowWhenNoActiveInsertsNew() {
        Long memberId = 10L;
        Long subId = 100L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(subId).build());
        given(notificationAllowRepository.findBySubIdAndCategory(subId, NotificationCategory.APP_SERVICE))
                .willReturn(Optional.empty());
        given(notificationAllowRepository.save(any()))
                .willAnswer(invocation -> {
                    NotificationAllow target = invocation.getArgument(0);
                    return NotificationAllow.builder()
                            .id(1L)
                            .subId(target.getSubId())
                            .notificationCategory(target.getNotificationCategory())
                            .notificationAllow(target.getNotificationAllow())
                            .isDeleted(false)
                            .build();
                });

        NotificationAllowResponse response = updateNotificationAllowService.updateNotificationAllow(
                memberId,
                new UpdateNotificationAllowRequest(NotificationCategory.APP_SERVICE, true)
        );

        assertThat(ReflectionTestUtils.getField(response, "notificationCategory"))
                .isEqualTo(NotificationCategory.APP_SERVICE);
        assertThat(ReflectionTestUtils.getField(response, "notificationAllow"))
                .isEqualTo(true);
    }

    @Test
    @DisplayName("disallow request soft deletes active record")
    void disallowSoftDeletesActiveRecord() {
        Long memberId = 10L;
        Long subId = 100L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(subId).build());
        given(notificationAllowRepository.findBySubIdAndCategory(subId, NotificationCategory.DATA))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .id(3L)
                        .subId(subId)
                        .notificationCategory(NotificationCategory.DATA)
                        .notificationAllow(true)
                        .isDeleted(false)
                        .build()));
        given(notificationAllowRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        NotificationAllowResponse response = updateNotificationAllowService.updateNotificationAllow(
                memberId,
                new UpdateNotificationAllowRequest(NotificationCategory.DATA, false)
        );

        assertThat(ReflectionTestUtils.getField(response, "notificationCategory"))
                .isEqualTo(NotificationCategory.DATA);
        assertThat(ReflectionTestUtils.getField(response, "notificationAllow"))
                .isEqualTo(false);

        ArgumentCaptor<NotificationAllow> captor = ArgumentCaptor.forClass(NotificationAllow.class);
        then(notificationAllowRepository).should().save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(3L);
        assertThat(captor.getValue().getNotificationAllow()).isFalse();
        assertThat(captor.getValue().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("disallow request without active record returns false response")
    void disallowWithoutActiveRecordReturnsFalse() {
        Long memberId = 10L;
        Long subId = 100L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(subId).build());
        given(notificationAllowRepository.findBySubIdAndCategory(subId, NotificationCategory.PRESENT))
                .willReturn(Optional.empty());

        NotificationAllowResponse response = updateNotificationAllowService.updateNotificationAllow(
                memberId,
                new UpdateNotificationAllowRequest(NotificationCategory.PRESENT, false)
        );

        assertThat(ReflectionTestUtils.getField(response, "notificationCategory"))
                .isEqualTo(NotificationCategory.PRESENT);
        assertThat(ReflectionTestUtils.getField(response, "notificationAllow"))
                .isEqualTo(false);
        then(notificationAllowRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("update notification allow fail when subscription not found")
    void updateNotificationAllowFailWhenSubscriptionNotFound() {
        Long memberId = 10L;
        given(subscriptionService.findByMemberId(memberId))
                .willThrow(new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        assertThatThrownBy(() -> updateNotificationAllowService.updateNotificationAllow(
                memberId,
                new UpdateNotificationAllowRequest(NotificationCategory.DATA, true)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
