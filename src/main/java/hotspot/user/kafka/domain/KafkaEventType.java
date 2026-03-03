package hotspot.user.kafka.domain;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;

public enum KafkaEventType {
    USAGE_THRESHOLD,
    TIME_WINDOW_POLICY,
    IMMEDIATE_BLOCK,
    SERVICE_ACCESS,
    PRESENT_DATA,
    FAMILY_CREATE,
    FAMILY_MEMBER_ADD,
    FAMILY_MEMBER_REMOVE,
    ;

    public static KafkaEventType from(String raw) {
        try {
            return KafkaEventType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        }
    }
}
