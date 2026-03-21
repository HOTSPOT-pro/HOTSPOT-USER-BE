package hotspot.user.outbox.notificationOutbox.domain.event;

public record ServiceAccessAlertOutboxEvent(
        Long subId,
        Long familyId,
        String serviceName,
        AlertAction action
) {
}
