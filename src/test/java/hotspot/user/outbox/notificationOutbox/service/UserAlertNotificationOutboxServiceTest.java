package hotspot.user.outbox.notificationOutbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.AlertAction;
import hotspot.user.outbox.notificationOutbox.domain.event.PolicyAlertOutboxEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.PresentDataGiftedOutboxEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.ServiceAccessAlertOutboxEvent;
import hotspot.user.policy.domain.PolicyType;

@ExtendWith(MockitoExtension.class)
class UserAlertNotificationOutboxServiceTest {

    @Mock
    private NotificationOutboxEventAppender outboxEventAppender;

    @InjectMocks
    private UserAlertNotificationOutboxService userAlertNotificationOutboxService;

    @Test
    @DisplayName("appendPolicyAlert emits TIME_WINDOW_POLICY event")
    void appendPolicyAlertWithScheduledType() {
        userAlertNotificationOutboxService.appendPolicyAlert(
                new PolicyAlertOutboxEvent(101L, 11L, "study-time", PolicyType.SCHEDULED, AlertAction.APPLIED)
        );

        assertOutbox(
                "101",
                "TIME_WINDOW_POLICY",
                "TIME_WINDOW_POLICY",
                "APPLIED",
                "study-time",
                null,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("appendPolicyAlert emits IMMEDIATE_BLOCK event")
    void appendPolicyAlertWithImmediateType() {
        userAlertNotificationOutboxService.appendPolicyAlert(
                new PolicyAlertOutboxEvent(202L, 22L, "night-block", PolicyType.ONCE, AlertAction.RELEASED)
        );

        assertOutbox("202", "IMMEDIATE_BLOCK", "IMMEDIATE_BLOCK", "RELEASED", "night-block", null, null, null, null);
    }

    @Test
    @DisplayName("appendServiceAccessAlert emits service access applied event")
    void appendServiceAccessAlertApplied() {
        userAlertNotificationOutboxService.appendServiceAccessAlert(
                new ServiceAccessAlertOutboxEvent(303L, 33L, "YouTube", AlertAction.APPLIED)
        );

        assertOutbox("303", "SERVICE_ACCESS", "SERVICE_ACCESS", "APPLIED", null, "YouTube", null, null, null);
    }

    @Test
    @DisplayName("appendServiceAccessAlert uses familyId as aggregateId when subId missing")
    void appendServiceAccessAlertReleasedUsesFamilyIdAsAggregateIdWhenSubIdMissing() {
        userAlertNotificationOutboxService.appendServiceAccessAlert(
                new ServiceAccessAlertOutboxEvent(null, 44L, "Instagram", AlertAction.RELEASED)
        );

        assertOutbox(
                "44",
                "SERVICE_ACCESS",
                "SERVICE_ACCESS",
                "RELEASED",
                null,
                "Instagram",
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("appendPresentDataGiftedAlert emits present data event")
    void appendPresentDataGiftedAlert() {
        userAlertNotificationOutboxService.appendPresentDataGiftedAlert(
                new PresentDataGiftedOutboxEvent(505L, 55L, "Alice", "1GB", "gift-1")
        );

        assertOutbox("505", "PRESENT_DATA", "PRESENT_DATA", "GIFTED", null, null, "Alice", "1GB", "gift-1");
    }

    private void assertOutbox(
            String expectedAggregateId,
            String expectedType,
            String expectedEventType,
            String expectedAlertType,
            String expectedPolicyName,
            String expectedServiceName,
            String expectedSenderName,
            String expectedAmount,
            String expectedGiftId
    ) {
        ArgumentCaptor<String> aggregateTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> aggregateIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        then(outboxEventAppender).should().append(
                aggregateTypeCaptor.capture(),
                aggregateIdCaptor.capture(),
                typeCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(aggregateTypeCaptor.getValue()).isEqualTo("user-alert");
        assertThat(aggregateIdCaptor.getValue()).isEqualTo(expectedAggregateId);
        assertThat(typeCaptor.getValue()).isEqualTo(expectedType);

        UserAlertEvent event = (UserAlertEvent) payloadCaptor.getValue();
        assertThat(event.alertId()).isNotBlank();
        assertThat(event.createdTime()).isNotNull();
        assertThat(event.eventType()).isEqualTo(expectedEventType);
        assertThat(event.alertType()).isEqualTo(expectedAlertType);
        assertThat(event.policyName()).isEqualTo(expectedPolicyName);
        assertThat(event.serviceName()).isEqualTo(expectedServiceName);
        assertThat(event.presentSenderName()).isEqualTo(expectedSenderName);
        assertThat(event.presentAmount()).isEqualTo(expectedAmount);
        assertThat(event.giftId()).isEqualTo(expectedGiftId);
    }
}
