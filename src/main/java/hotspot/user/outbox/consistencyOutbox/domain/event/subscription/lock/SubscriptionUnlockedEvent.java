package hotspot.user.outbox.consistencyOutbox.domain.event.subscription.lock;

import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record SubscriptionUnlockedEvent(
        Long subId,
        String eventId
) implements DomainEvent {

    @Override
    public String type() {
        return "SUBSCRIPTION_UNLOCKED";
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
