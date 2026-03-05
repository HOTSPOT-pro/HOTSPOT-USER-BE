package hotspot.user.dispatch.sms.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.notification.domain.Notification;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsDispatchQueuePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final PresentDataRepository presentDataRepository;

    @Value("${app.topics.sms-dispatch}")
    private String smsDispatchTopic;

    public record SmsDispatchMetadata(
            String planName,
            String presentSenderName
    ) {
    }

    // sourceEvent 기반으로 SMS 본문 확장값(요금제명/선물 발신자명)을 한 번 조회한다.
    public SmsDispatchMetadata resolveMetadata(UserAlertEvent sourceEvent) {
        String planName = resolvePlanName(sourceEvent.subId());
        String presentSenderName = resolvePresentSenderName(sourceEvent.giftId(), sourceEvent.presentSenderName());
        return new SmsDispatchMetadata(planName, presentSenderName);
    }

    // 미리 조회한 메타데이터를 사용해 SMS 디스패치 Kafka 토픽에 전송한다.
    public void enqueue(
            Notification notification,
            UserAlertEvent sourceEvent,
            SmsDispatchMetadata metadata
    ) {
        try {
            SmsDispatchCommand command = SmsDispatchCommand.from(
                    notification,
                    sourceEvent,
                    metadata.planName(),
                    metadata.presentSenderName()
            );
            String payload = objectMapper.writeValueAsString(command);
            String key = notification.getSubId() + ":" + notification.getId();
            kafkaTemplate.send(smsDispatchTopic, key, payload);
        } catch (Exception ex) {
            throw new ApplicationException(SmsErrorCode.SMS_LISTENER_FAILED, ex);
        }
    }

    // subId로 구독 정보를 조회해 SMS 본문에 사용할 요금제명을 반환한다.
    private String resolvePlanName(Long subId) {
        if (subId == null) {
            return null;
        }
        return subscriptionRepository.findById(subId)
                .map(subscription -> subscription.getPlan())
                .map(plan -> plan.getName())
                .orElse(null);
    }

    // giftId로 선물 보낸 사람 이름을 조회하고 없으면 이벤트값을 사용한다.
    private String resolvePresentSenderName(String giftIdRaw, String fallbackSenderName) {
        Long giftId = parseGiftId(giftIdRaw);
        if (giftId == null) {
            return fallbackSenderName;
        }

        Map<Long, String> giverNames = presentDataRepository.findGiftGiverNames(List.of(giftId));
        return giverNames.getOrDefault(giftId, fallbackSenderName);
    }

    // 문자열 giftId를 Long으로 파싱하고 실패하면 null을 반환한다.
    private Long parseGiftId(String giftIdRaw) {
        if (giftIdRaw == null || giftIdRaw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(giftIdRaw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
