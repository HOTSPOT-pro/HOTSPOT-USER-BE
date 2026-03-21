package hotspot.user.dispatch.sms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class SmsProperties {

    private final boolean enabled;
    private final String provider;
    private final String from;
    private final String apiKey;
    private final String apiSecret;
    private final String apiBaseUrl;
    private final String sendPath;

    public SmsProperties(
            @Value("${sms.enabled:false}") boolean enabled,
            @Value("${sms.provider:noop}") String provider,
            @Value("${sms.from:}") String from,
            @Value("${sms.api-key:}") String apiKey,
            @Value("${sms.api-secret:}") String apiSecret,
            @Value("${sms.api-base-url:https://api.solapi.com}") String apiBaseUrl,
            @Value("${sms.send-path:/messages/v4/send-many/detail}") String sendPath
    ) {
        this.enabled = enabled;
        this.provider = provider;
        this.from = from;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiBaseUrl = apiBaseUrl;
        this.sendPath = sendPath;
    }
}
