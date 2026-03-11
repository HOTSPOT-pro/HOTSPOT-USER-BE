package hotspot.user.dispatch.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.dispatch.sms.support.SolapiRequestFactory;

class SolapiRequestFactoryTest {

    private final SolapiRequestFactory factory = new SolapiRequestFactory();

    @Test
    @DisplayName("builds send url safely for slash combinations")
    void buildSendUrl() {
        assertThat(factory.buildSendUrl("https://api.solapi.com", "/messages/v4/send-many/detail"))
                .isEqualTo("https://api.solapi.com/messages/v4/send-many/detail");
        assertThat(factory.buildSendUrl("https://api.solapi.com/", "/messages/v4/send-many/detail"))
                .isEqualTo("https://api.solapi.com/messages/v4/send-many/detail");
        assertThat(factory.buildSendUrl("https://api.solapi.com", "messages/v4/send-many/detail"))
                .isEqualTo("https://api.solapi.com/messages/v4/send-many/detail");
    }

    @Test
    @DisplayName("builds payload with digits-only phone numbers")
    void buildPayload() {
        Map<String, Object> payload = factory.buildPayload("010-1111-2222", "010-3333-4444", "hello");

        assertThat(payload).containsKey("messages");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) payload.get("messages");
        assertThat(messages).hasSize(1);
        Map<String, String> message = messages.get(0);
        assertThat(message.get("from")).isEqualTo("01011112222");
        assertThat(message.get("to")).isEqualTo("01033334444");
        assertThat(message.get("text")).isEqualTo("hello");
    }
}
