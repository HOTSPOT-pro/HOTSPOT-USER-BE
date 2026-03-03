package hotspot.user.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.kafka.dto.UserAlertEvent;

class LenientJsonOrStringDeserializerTest {

    @Test
    @DisplayName("Deserializes plain JSON object payload")
    void shouldDeserializePlainJsonPayload() {
        // given
        LenientJsonOrStringDeserializer<UserAlertEvent> deserializer =
                new LenientJsonOrStringDeserializer<>(UserAlertEvent.class);
        String plainJson = """
                {
                  "alertId": "alert-plain",
                  "eventType": "PRESENT_DATA",
                  "alertType": "GIFTED",
                  "subId": 1000006,
                  "familyId": 250001,
                  "presentSenderName": "sender",
                  "presentAmount": "5GB",
                  "giftId": "69687",
                  "createdTime": "2026-03-03T07:33:51.891936199"
                }
                """;

        // when
        UserAlertEvent event = deserializer.deserialize(
                "user-alert-events",
                plainJson.getBytes(StandardCharsets.UTF_8)
        );

        // then
        assertThat(event).isNotNull();
        assertThat(event.alertId()).isEqualTo("alert-plain");
        assertThat(event.eventType()).isEqualTo("PRESENT_DATA");
        assertThat(event.alertType()).isEqualTo("GIFTED");
        assertThat(event.subId()).isEqualTo(1000006L);
        assertThat(event.familyId()).isEqualTo(250001L);
        assertThat(event.createdTime()).isEqualTo(LocalDateTime.parse("2026-03-03T07:33:51.891936199"));
    }

    @Test
    @DisplayName("Deserializes JSON string payload that wraps a JSON object")
    void shouldDeserializeWrappedJsonStringPayload() {
        // given
        LenientJsonOrStringDeserializer<UserAlertEvent> deserializer =
                new LenientJsonOrStringDeserializer<>(UserAlertEvent.class);
        String wrappedJsonString =
                "\"{\\\"alertId\\\":\\\"alert-wrapped\\\",\\\"eventType\\\":\\\"PRESENT_DATA\\\","
                        + "\\\"alertType\\\":\\\"GIFTED\\\",\\\"subId\\\":1000005,\\\"familyId\\\":250001,"
                        + "\\\"presentSenderName\\\":\\\"sender\\\",\\\"presentAmount\\\":\\\"1GB\\\","
                        + "\\\"giftId\\\":\\\"69688\\\",\\\"createdTime\\\":\\\"2026-03-03T08:37:15.563975739\\\"}\"";

        // when
        UserAlertEvent event = deserializer.deserialize(
                "user-alert-events",
                wrappedJsonString.getBytes(StandardCharsets.UTF_8)
        );

        // then
        assertThat(event).isNotNull();
        assertThat(event.alertId()).isEqualTo("alert-wrapped");
        assertThat(event.eventType()).isEqualTo("PRESENT_DATA");
        assertThat(event.alertType()).isEqualTo("GIFTED");
        assertThat(event.subId()).isEqualTo(1000005L);
        assertThat(event.familyId()).isEqualTo(250001L);
        assertThat(event.createdTime()).isEqualTo(LocalDateTime.parse("2026-03-03T08:37:15.563975739"));
    }
}
