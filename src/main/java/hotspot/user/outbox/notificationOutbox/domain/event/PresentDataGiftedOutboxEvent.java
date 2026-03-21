package hotspot.user.outbox.notificationOutbox.domain.event;

public record PresentDataGiftedOutboxEvent(
        Long targetSubId,
        Long familyId,
        String senderName,
        String presentAmount,
        String giftId
) {
}
