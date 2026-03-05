package hotspot.user.outbox.consistencyOutbox.domain.event.family.policyChanged;

import java.util.List;

import hotspot.user.outbox.consistencyOutbox.domain.PolicyPayload;
import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyPolicyChangedEvent(
        String eventId,
        Long familyId,
        String type,
        List<Long> subIds,
        PolicyPayload policy
) implements DomainEvent {

    @Override
    public String type() {
        return type;
    }

    @Override
    public String aggregateType() {
        return "family";
    }

    @Override
    public String aggregateId() {
        return familyId.toString();
    }
}
