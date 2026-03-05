package hotspot.user.outbox.consistencyOutbox.domain.event.family.policyDeleted;

import java.util.List;

import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyPolicyDeletedEvent(
        String eventId,
        Long familyId,
        String type,
        List<PolicyTarget> policies
) implements DomainEvent {

    public record PolicyTarget(
            Long policyId,
            List<Long> subIds
    ) {}

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
