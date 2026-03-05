package hotspot.user.outbox.consistencyOutbox.domain.event.family.policyDeleted;

import java.util.List;

import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyPolicyDeletedEvent(
        String eventId,
        Long familyId,
        String type,
        List<Long> subIds,
        Long policyId
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
