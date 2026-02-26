package hotspot.user.kafka.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        Long subId,
        Long familyId,
        String threshold,
        String policyName,
        String serviceName,
        String presentSenderName,
        String presentAmount,
        String giftId,
        String targetName,
        LocalDateTime createdTime
) {
}
