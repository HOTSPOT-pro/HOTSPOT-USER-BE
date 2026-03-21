package hotspot.user.dispatch.sms.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import hotspot.user.dispatch.sms.config.SmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "noop", matchIfMissing = true)
@RequiredArgsConstructor
public class NoopSmsSender implements SmsSenderPort {

    private final SmsProperties smsProperties;

    @Override
    // 실제 발송 대신 로그만 남기는 NOOP 발송을 수행한다.
    public void send(String to, String message) {
        log.info(
                "NOOP SMS send. provider={}, from={}, to={}, message={}",
                smsProperties.getProvider(),
                smsProperties.getFrom(),
                to,
                message
        );
    }
}
