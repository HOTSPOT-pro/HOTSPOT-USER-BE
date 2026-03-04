package hotspot.user.dispatch.sms.service;

import org.springframework.stereotype.Component;

import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.notification.domain.Notification;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsMessageBuilder {

    private final SmsProperties smsProperties;

    // 발신번호 접두어를 포함한 SMS 본문 문자열을 생성한다.
    public String build(Notification notification) {
        return "[" + smsProperties.getFrom() + "] " + notification.getTitle() + " - " + notification.getContent();
    }
}
