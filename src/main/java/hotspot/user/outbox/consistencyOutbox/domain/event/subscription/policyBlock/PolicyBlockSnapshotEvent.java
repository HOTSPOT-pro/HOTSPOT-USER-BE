package hotspot.user.outbox.consistencyOutbox.domain.event.subscription.policyBlock;

import java.util.List;

import hotspot.user.outbox.consistencyOutbox.domain.PolicyPayload;
import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record PolicyBlockSnapshotEvent(
        String type,
        Long subId,
        List<PolicyPayload> policies,
        String eventId
) implements DomainEvent {

    @Override
    public String aggregateType() {
        return "subscription";
    }

    @Override
    public String aggregateId() {
        return subId.toString();
    }
}
