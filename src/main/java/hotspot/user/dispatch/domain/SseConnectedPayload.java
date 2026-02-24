package hotspot.user.dispatch.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Builder;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
public class SseConnectedPayload {
    private Long subId;
    private String lastEventId;
    private LocalDateTime connectedTime;
}
