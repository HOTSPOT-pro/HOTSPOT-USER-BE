package hotspot.user.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import hotspot.user.kafka.dto.UserAlertEvent;

class KafkaConsumerConfigTest {

    @Test
    @DisplayName("Deserializes sample user alert payload into UserAlertEvent")
    void shouldDeserializeSampleUserAlertEvent() {
        // given
        JsonDeserializer<UserAlertEvent> jsonDeserializer = new JsonDeserializer<>(UserAlertEvent.class);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        String sampleMessage = """
                {
                  "alertId": "alert-1",
                  "eventType": "USAGE",
                  "alertType": "THRESHOLD_EXCEEDED",
                  "threshold": "80",
                  "createdTime": "2026-02-23T01:00:00",
                  "subId": 1001,
                  "familyId": 2002,
                  "giftId": "3003",
                  "targetNames": ["Kid A", "Kid B"]
                }
                """;

        // when
        UserAlertEvent event = jsonDeserializer.deserialize(
                "user-alert-events",
                sampleMessage.getBytes(StandardCharsets.UTF_8)
        );

        // then
        assertThat(event).isNotNull();
        assertThat(event.alertId()).isEqualTo("alert-1");
        assertThat(event.eventType()).isEqualTo("USAGE");
        assertThat(event.alertType()).isEqualTo("THRESHOLD_EXCEEDED");
        assertThat(event.threshold()).isEqualTo("80");
        assertThat(event.createdTime()).isEqualTo(LocalDateTime.parse("2026-02-23T01:00:00"));
        assertThat(event.subId()).isEqualTo(1001L);
        assertThat(event.familyId()).isEqualTo(2002L);
        assertThat(event.giftId()).isEqualTo("3003");
        assertThat(event.targetNames()).containsExactly("Kid A", "Kid B");
    }

    @Test
    @DisplayName("Builds consumer factory and listener factory with manual ack and error handler")
    void shouldBuildKafkaConsumerAndListenerFactory() {
        // given
        KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig("user-alert-consumer-group");

        KafkaProperties kafkaProperties = new KafkaProperties();
        kafkaProperties.setBootstrapServers(List.of("localhost:9092"));
        kafkaProperties.getConsumer().setGroupId("user-alert-consumer-group");
        SslBundles sslBundles = mock(SslBundles.class);

        // when
        ConsumerFactory<String, UserAlertEvent> consumerFactory =
                kafkaConsumerConfig.userAlertEventConsumerFactory(kafkaProperties, sslBundles);
        ConcurrentKafkaListenerContainerFactory<String, UserAlertEvent> listenerFactory =
                kafkaConsumerConfig.userAlertKafkaListenerContainerFactory(consumerFactory);

        Map<String, Object> props = consumerFactory.getConfigurationProperties();

        // then
        assertThat(props.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("user-alert-consumer-group");
        assertThat(props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
        assertThat(listenerFactory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.MANUAL);
        assertThat(listenerFactory).isNotNull();
    }
}
