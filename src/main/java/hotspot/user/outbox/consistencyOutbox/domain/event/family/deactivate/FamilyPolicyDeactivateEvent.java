package hotspot.user.outbox.consistencyOutbox.domain.event.family.deactivate;

import java.util.List;

import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record FamilyPolicyDeactivateEvent(
        String eventId,
        Long familyId,
        String type,
        List<PolicyTarget> policies
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

    public record PolicyTarget(
            Long policyId,
            List<Long> subIds
    ) {}
}
