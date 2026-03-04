package hotspot.user.dispatch.sms.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.dispatch.sms.config.SmsProperties;

class NoopSmsSenderTest {

    @Test
    @DisplayName("send does not throw")
    void sendNoop() {
        NoopSmsSender sender = new NoopSmsSender(
                new SmsProperties(true, false, false, false, false, "noop", "01012345678")
        );

        sender.send("01012345678", "hello");
    }
}
