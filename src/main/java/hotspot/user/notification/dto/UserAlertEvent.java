package hotspot.user.notification.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        String threshold,
        Instant occurredAt,
        Long subId,
        Long familyId,
        String giftId,
        long remainingBytes,
        int remainingPct,
        String sourceEventId
) {
}
