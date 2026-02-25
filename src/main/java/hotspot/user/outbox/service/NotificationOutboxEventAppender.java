package hotspot.user.outbox.service;

import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.OutboxErrorCode;
import hotspot.user.outbox.infrastructure.NotificationOutboxEventJpaRepository;
import hotspot.user.outbox.infrastructure.entity.NotificationOutboxEventEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationOutboxEventAppender {

    private final NotificationOutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    // 단일 outbox 이벤트 레코드를 outbox_event 테이블에 저장한다.
    public void append(String aggregateType, String aggregateId, String type, Object payload) {
        outboxEventJpaRepository.save(
                NotificationOutboxEventEntity.builder()
                        .id(UUID.randomUUID())
                        .aggregateType(aggregateType)
                        .aggregateId(aggregateId)
                        .type(type)
                        .payload(toJson(payload))
                        .build()
        );
    }

    // payload 객체를 JSON 문자열로 직렬화한다.
    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_PAYLOAD_SERIALIZATION_FAILED, ex);
        }
    }
}
