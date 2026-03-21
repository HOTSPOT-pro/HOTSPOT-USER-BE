package hotspot.user.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAlertEvent(
        String alertId,
        String eventType,
        String alertType,
        Long subId,
        Long familyId,
        String threshold,
        String providedAmount,
        String usedPercent,
        String usedAmount,
        String policyName,
        String serviceName,
        String presentSenderName,
        String presentAmount,
        String giftId,
        List<String> targetNames,
        LocalDateTime createdTime
) {
}
