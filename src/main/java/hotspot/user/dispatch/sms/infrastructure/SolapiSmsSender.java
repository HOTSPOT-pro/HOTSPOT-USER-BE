package hotspot.user.dispatch.sms.infrastructure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.dispatch.sms.support.SolapiAuthHeaderFactory;
import hotspot.user.dispatch.sms.support.SolapiRequestFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "solapi")
public class SolapiSmsSender implements SmsSenderPort {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final SmsProperties smsProperties;
    private final ObjectMapper objectMapper;
    private final SolapiAuthHeaderFactory authHeaderFactory;
    private final SolapiRequestFactory requestFactory;
    private final HttpClient httpClient;

    public SolapiSmsSender(
            SmsProperties smsProperties,
            ObjectMapper objectMapper,
            SolapiAuthHeaderFactory authHeaderFactory,
            SolapiRequestFactory requestFactory,
            HttpClient httpClient
    ) {
        this.smsProperties = smsProperties;
        this.objectMapper = objectMapper;
        this.authHeaderFactory = authHeaderFactory;
        this.requestFactory = requestFactory;
        this.httpClient = httpClient;
    }

    @Override
    // Solapi API를 호출해 실제 SMS 발송을 수행한다.
    public void send(String to, String message) {
        validateCredentials();
        try {
            String authorization = authHeaderFactory.create(smsProperties.getApiKey(), smsProperties.getApiSecret());

            String payload = objectMapper.writeValueAsString(
                    requestFactory.buildPayload(smsProperties.getFrom(), to, message)
            );
            String sendUrl = requestFactory.buildSendUrl(smsProperties.getApiBaseUrl(), smsProperties.getSendPath());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sendUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApplicationException(SmsErrorCode.SMS_PROVIDER_REQUEST_FAILED);
            }

        } catch (Exception ex) {
            if (ex instanceof ApplicationException) {
                throw (ApplicationException) ex;
            }
            throw new ApplicationException(SmsErrorCode.SMS_PROVIDER_REQUEST_FAILED, ex);
        }
    }

    // Solapi 인증/발신 설정 필수값이 존재하는지 검증한다.
    private void validateCredentials() {
        if (isBlank(smsProperties.getApiKey())) {
            throw new ApplicationException(SmsErrorCode.SMS_PROVIDER_CONFIG_INVALID);
        }
        if (isBlank(smsProperties.getApiSecret())) {
            throw new ApplicationException(SmsErrorCode.SMS_PROVIDER_CONFIG_INVALID);
        }
        if (isBlank(smsProperties.getFrom())) {
            throw new ApplicationException(SmsErrorCode.SMS_PROVIDER_CONFIG_INVALID);
        }
    }

    // 문자열이 null 또는 공백인지 반환한다.
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
