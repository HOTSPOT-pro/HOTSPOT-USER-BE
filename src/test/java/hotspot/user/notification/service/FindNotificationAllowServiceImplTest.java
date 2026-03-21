package hotspot.user.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.notification.controller.response.NotificationAllowListResponse;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class FindNotificationAllowServiceImplTest {

    @Mock
    private NotificationAllowRepository notificationAllowRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private FindNotificationAllowServiceImpl findNotificationAllowService;

    @Test
    @DisplayName("find notification allows success")
    void findNotificationAllowsSuccess() {
        Long memberId = 10L;
        Long subId = 100L;
        given(subscriptionService.findByMemberId(memberId))
                .willReturn(Subscription.builder().id(subId).build());
        given(notificationAllowRepository.findAllBySubId(subId))
                .willReturn(List.of(
                        NotificationAllow.builder()
                                .id(1L)
                                .subId(subId)
                                .notificationCategory(NotificationCategory.DATA)
                                .notificationAllow(true)
                                .isDeleted(false)
                                .build()
                ));

        NotificationAllowListResponse response = findNotificationAllowService.findNotificationAllows(memberId);

        @SuppressWarnings("unchecked")
        List<Object> allows = (List<Object>) ReflectionTestUtils.getField(response, "notificationAllows");
        assertThat(allows).hasSize(4);
        Object first = allows.get(0);
        assertThat(ReflectionTestUtils.getField(first, "notificationCategory")).isEqualTo(NotificationCategory.DATA);
        assertThat(ReflectionTestUtils.getField(first, "notificationAllow")).isEqualTo(true);
    }

    @Test
    @DisplayName("find notification allows fail when subscription not found")
    void findNotificationAllowsFailWhenSubscriptionNotFound() {
        Long memberId = 10L;
        given(subscriptionService.findByMemberId(memberId))
                .willThrow(new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        assertThatThrownBy(() -> findNotificationAllowService.findNotificationAllows(memberId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
