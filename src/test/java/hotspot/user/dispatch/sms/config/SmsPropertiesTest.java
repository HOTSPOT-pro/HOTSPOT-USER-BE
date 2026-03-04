package hotspot.user.dispatch.sms.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SmsPropertiesTest {

    @Test
    @DisplayName("keeps configured sms properties")
    void propertiesValues() {
        SmsProperties properties = new SmsProperties(true, false, false, false, false, "noop", "01012345678");

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isEnabled2()).isFalse();
        assertThat(properties.isEnabled3()).isFalse();
        assertThat(properties.isEnabled4()).isFalse();
        assertThat(properties.isEnabled5()).isFalse();
        assertThat(properties.getProvider()).isEqualTo("noop");
        assertThat(properties.getFrom()).isEqualTo("01012345678");
    }
}
