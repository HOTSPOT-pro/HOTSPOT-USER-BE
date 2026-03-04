package hotspot.user.dispatch.sms.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SmsPropertiesTest {

    @Test
    @DisplayName("keeps configured sms properties")
    void propertiesValues() {
        SmsProperties properties = new SmsProperties(
                true, "noop", "01012345678",
                "api-key", "api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
        );

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getProvider()).isEqualTo("noop");
        assertThat(properties.getFrom()).isEqualTo("01012345678");
        assertThat(properties.getApiKey()).isEqualTo("api-key");
        assertThat(properties.getApiSecret()).isEqualTo("api-secret");
        assertThat(properties.getApiBaseUrl()).isEqualTo("https://api.solapi.com");
        assertThat(properties.getSendPath()).isEqualTo("/messages/v4/send-many/detail");
    }
}
