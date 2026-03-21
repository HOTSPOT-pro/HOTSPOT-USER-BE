package hotspot.user.outbox.notificationOutbox.domain.event;

import hotspot.user.policy.domain.PolicyType;

public record PolicyAlertOutboxEvent(
        Long subId,
        Long familyId,
        String policyName,
        PolicyType policyType,
        AlertAction action
) {
}
