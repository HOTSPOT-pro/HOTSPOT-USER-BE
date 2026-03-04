package hotspot.user.dispatch.sms.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.dispatch.sms.config.SmsProperties;

class NoopSmsSenderTest {

    @Test
    @DisplayName("send does not throw")
    void sendNoop() {
        NoopSmsSender sender = new NoopSmsSender(
                new SmsProperties(
                        true, "noop", "01012345678",
                        "api-key", "api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
                )
        );

        sender.send("01012345678", "hello");
    }
}
