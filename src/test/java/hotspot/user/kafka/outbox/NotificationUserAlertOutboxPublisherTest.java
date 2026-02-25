package hotspot.user.kafka.outbox;

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
import hotspot.user.outbox.service.NotificationOutboxEventAppender;
import hotspot.user.policy.domain.PolicyType;

@ExtendWith(MockitoExtension.class)
class NotificationUserAlertOutboxPublisherTest {

    @Mock
    private NotificationOutboxEventAppender outboxEventAppender;

    @InjectMocks
    private NotificationUserAlertOutboxPublisher publisher;

    @Test
    @DisplayName("publishPolicyApplied: SCHEDULED 정책은 TIME_WINDOW_POLICY 이벤트로 저장한다")
    void publishPolicyAppliedWithScheduledType() {
        publisher.publishPolicyApplied(101L, 11L, "study-time", PolicyType.SCHEDULED);

        assertOutbox("101", "POLICY_APPLIED", "TIME_WINDOW_POLICY", "APPLIED", "study-time", null, null, null, null);
    }

    @Test
    @DisplayName("publishPolicyReleased: ONCE 정책은 IMMEDIATE_BLOCK 이벤트로 저장한다")
    void publishPolicyReleasedWithImmediateType() {
        publisher.publishPolicyReleased(202L, 22L, "night-block", PolicyType.ONCE);

        assertOutbox("202", "POLICY_RELEASED", "IMMEDIATE_BLOCK", "RELEASED", "night-block", null, null, null, null);
    }

    @Test
    @DisplayName("publishServiceAccessApplied: 서비스 차단 적용 이벤트를 저장한다")
    void publishServiceAccessApplied() {
        publisher.publishServiceAccessApplied(303L, 33L, "YouTube");

        assertOutbox("303", "SERVICE_ACCESS_APPLIED", "SERVICE_ACCESS", "APPLIED", null, "YouTube", null, null, null);
    }

    @Test
    @DisplayName("publishServiceAccessReleased: subId가 없으면 familyId를 aggregateId로 사용한다")
    void publishServiceAccessReleasedUsesFamilyIdAsAggregateIdWhenSubIdMissing() {
        publisher.publishServiceAccessReleased(null, 44L, "Instagram");

        assertOutbox(
                "44",
                "SERVICE_ACCESS_RELEASED",
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
    @DisplayName("publishPresentDataGifted: 선물 데이터 이벤트를 저장한다")
    void publishPresentDataGifted() {
        publisher.publishPresentDataGifted(505L, 55L, "Alice", "1GB", "gift-1");

        assertOutbox("505", "PRESENT_DATA_GIFTED", "PRESENT_DATA", "GIFTED", null, null, "Alice", "1GB", "gift-1");
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
        assertThat(event.sourceEventId()).isEqualTo(event.alertId());
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.eventType()).isEqualTo(expectedEventType);
        assertThat(event.alertType()).isEqualTo(expectedAlertType);
        assertThat(event.policyName()).isEqualTo(expectedPolicyName);
        assertThat(event.serviceName()).isEqualTo(expectedServiceName);
        assertThat(event.presentSenderName()).isEqualTo(expectedSenderName);
        assertThat(event.presentAmount()).isEqualTo(expectedAmount);
        assertThat(event.giftId()).isEqualTo(expectedGiftId);
    }
}
