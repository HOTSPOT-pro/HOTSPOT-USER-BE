package hotspot.user.dispatch.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.support.SolapiAuthHeaderFactory;

class SolapiAuthHeaderFactoryTest {

    private final SolapiAuthHeaderFactory factory = new SolapiAuthHeaderFactory();

    @Test
    @DisplayName("creates hmac authorization header with required fields")
    void createHeader() {
        String header = factory.create("test-api-key", "test-api-secret");

        assertThat(header).startsWith("HMAC-SHA256 apiKey=test-api-key");
        assertThat(header).contains(", date=");
        assertThat(header).contains(", salt=");
        assertThat(header).contains(", signature=");
    }

    @Test
    @DisplayName("throws common sms auth error when secret is invalid")
    void createHeaderFails() {
        assertThatThrownBy(() -> factory.create("test-api-key", null))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getCode()).isEqualTo(SmsErrorCode.SMS_PROVIDER_AUTH_FAILED);
                });
    }
}
