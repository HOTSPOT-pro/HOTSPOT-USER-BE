package hotspot.user.dispatch.sms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.notification.domain.Notification;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsDispatchQueuePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.topics.sms-dispatch}")
    private String smsDispatchTopic;

    // 알림 정보를 SMS 디스패치 Kafka 토픽에 전송한다.
    public void enqueue(Notification notification) {
        try {
            SmsDispatchCommand command = SmsDispatchCommand.from(notification);
            String payload = objectMapper.writeValueAsString(command);
            String key = notification.getSubId() + ":" + notification.getId();
            kafkaTemplate.send(smsDispatchTopic, key, payload);
        } catch (Exception ex) {
            throw new ApplicationException(SmsErrorCode.SMS_LISTENER_FAILED, ex);
        }
    }
}
