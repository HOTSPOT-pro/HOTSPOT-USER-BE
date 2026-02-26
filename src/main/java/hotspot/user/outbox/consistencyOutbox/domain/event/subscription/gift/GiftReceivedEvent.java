package hotspot.user.outbox.consistencyOutbox.domain.event.subscription.gift;

import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;

public record GiftReceivedEvent(
        String type,
        Long receiverSubId,
        Long giverSubId,
        Long giftId,
        Long giftLimitBytes,
        Long giftAmountBytes,
        String yyyyMM,
        String yyyyMMDD,
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
        return receiverSubId.toString();
    }
}
