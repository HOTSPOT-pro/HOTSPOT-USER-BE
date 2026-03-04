package hotspot.user.dispatch.sms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class SmsProperties {

    private final boolean enabled;
    private final boolean enabled2;
    private final boolean enabled3;
    private final boolean enabled4;
    private final boolean enabled5;
    private final String provider;
    private final String from;

    // SMS 동작 여부/제공자/발신번호 설정값을 초기화한다.
    public SmsProperties(
            @Value("${sms.enabled:false}") boolean enabled,
            @Value("${sms.enabled2:false}") boolean enabled2,
            @Value("${sms.enabled3:false}") boolean enabled3,
            @Value("${sms.enabled4:false}") boolean enabled4,
            @Value("${sms.enabled5:false}") boolean enabled5,
            @Value("${sms.provider:noop}") String provider,
            @Value("${sms.from:}") String from
    ) {
        this.enabled = enabled;
        this.enabled2 = enabled2;
        this.enabled3 = enabled3;
        this.enabled4 = enabled4;
        this.enabled5 = enabled5;
        this.provider = provider;
        this.from = from;
    }
}
