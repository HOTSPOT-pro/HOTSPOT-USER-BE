package hotspot.user.outbox.consistencyOutbox.domain.event.subscription.app;

import java.util.List;

import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record AppBlockListUpdateEvent(
        String type,
        Long subId,
        List<Long> appIds,
        String eventId
) implements DomainEvent {

    @Override
    public String type() {
        return type;
    }

    @Override
    public String aggregateType() {
        return "subscription";
    }

    @Override
    public String aggregateId() {
        return subId.toString();
    }
}
