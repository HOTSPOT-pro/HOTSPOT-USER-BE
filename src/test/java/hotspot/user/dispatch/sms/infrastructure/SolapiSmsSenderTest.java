package hotspot.user.dispatch.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.dispatch.sms.support.SolapiAuthHeaderFactory;
import hotspot.user.dispatch.sms.support.SolapiRequestFactory;

@ExtendWith(MockitoExtension.class)
class SolapiSmsSenderTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    @Mock
    private SolapiAuthHeaderFactory authHeaderFactory;

    @Mock
    private SolapiRequestFactory requestFactory;

    @Test
    @DisplayName("calls solapi endpoint with hmac authorization when sending sms")
    void sendSuccess() throws Exception {
        SmsProperties properties = solapiProperties();
        SolapiSmsSender sender = new SolapiSmsSender(
                properties,
                objectMapper,
                authHeaderFactory,
                requestFactory,
                httpClient
        );
        given(authHeaderFactory.create("test-api-key", "test-api-secret")).willReturn("auth-header");
        given(requestFactory.buildSendUrl("https://api.solapi.com", "/messages/v4/send-many/detail"))
                .willReturn("https://api.solapi.com/messages/v4/send-many/detail");
        given(requestFactory.buildPayload("01012345678", "010-1234-5678", "test-message"))
                .willReturn(java.util.Map.of("messages", java.util.List.of(java.util.Map.of())));
        given(objectMapper.writeValueAsString(any())).willReturn("{\"messages\":[{}]}");
        given(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willReturn(response);
        given(response.statusCode()).willReturn(200);

        sender.send("010-1234-5678", "test-message");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        then(httpClient).should().send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest request = requestCaptor.getValue();

        assertThat(request.uri().toString()).isEqualTo("https://api.solapi.com/messages/v4/send-many/detail");
        assertThat(request.headers().firstValue("Authorization")).isPresent();
        assertThat(request.headers().firstValue("Authorization").orElse("")).isEqualTo("auth-header");
    }

    @Test
    @DisplayName("throws when solapi response is non-success")
    void sendFailsWhenNonSuccessResponse() throws Exception {
        SmsProperties properties = solapiProperties();
        SolapiSmsSender sender = new SolapiSmsSender(
                properties,
                objectMapper,
                authHeaderFactory,
                requestFactory,
                httpClient
        );
        given(authHeaderFactory.create("test-api-key", "test-api-secret")).willReturn("auth-header");
        given(requestFactory.buildSendUrl("https://api.solapi.com", "/messages/v4/send-many/detail"))
                .willReturn("https://api.solapi.com/messages/v4/send-many/detail");
        given(requestFactory.buildPayload("01012345678", "01012345678", "test-message"))
                .willReturn(java.util.Map.of("messages", java.util.List.of(java.util.Map.of())));
        given(objectMapper.writeValueAsString(any())).willReturn("{\"messages\":[{}]}");
        given(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willReturn(response);
        given(response.statusCode()).willReturn(400);

        assertThatThrownBy(() -> sender.send("01012345678", "test-message"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getCode()).isEqualTo(SmsErrorCode.SMS_PROVIDER_REQUEST_FAILED);
                });
    }

    @Test
    @DisplayName("throws when required solapi credential is missing")
    void sendFailsWhenCredentialMissing() {
        SmsProperties properties = new SmsProperties(
                true, "solapi", "01012345678",
                "", "test-api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
        );
        SolapiSmsSender sender = new SolapiSmsSender(
                properties,
                objectMapper,
                authHeaderFactory,
                requestFactory,
                httpClient
        );

        assertThatThrownBy(() -> sender.send("01012345678", "test-message"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getCode()).isEqualTo(SmsErrorCode.SMS_PROVIDER_CONFIG_INVALID);
                });
    }

    private SmsProperties solapiProperties() {
        return new SmsProperties(
                true, "solapi", "01012345678",
                "test-api-key", "test-api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
        );
    }
}
