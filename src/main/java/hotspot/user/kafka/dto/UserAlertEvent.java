package hotspot.user.kafka.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        String threshold,
        String policyName,
        String serviceName,
        String presentSenderName,
        String presentAmount,
        Instant occurredAt,
        Long subId,
        Long familyId,
        String giftId,
        long remainingBytes,
        int remainingPct,
        String sourceEventId
) {
}
