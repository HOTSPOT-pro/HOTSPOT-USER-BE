package hotspot.user.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.kafka.dto.UserAlertEvent;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    private static final int CONCURRENCY = 6;
    private static final long RETRY_INTERVAL_MS = 1_000L;
    private static final long RETRY_MAX_ATTEMPTS = 2L;

    private final String userAlertConsumerGroup;
    private final String smsDispatchConsumerGroup;

    public KafkaConsumerConfig(
            @Value("${app.consumer-groups.user-alert}") String userAlertConsumerGroup,
            @Value("${app.consumer-groups.sms-dispatch}") String smsDispatchConsumerGroup
    ) {
        this.userAlertConsumerGroup = userAlertConsumerGroup;
        this.smsDispatchConsumerGroup = smsDispatchConsumerGroup;
    }

    @Bean
    public ConsumerFactory<String, UserAlertEvent> userAlertEventConsumerFactory(
            KafkaProperties kafkaProperties,
            SslBundles sslBundles
    ) {
        return KafkaConsumerFactorySupport.createConsumerFactory(
                kafkaProperties,
                sslBundles,
                UserAlertEvent.class,
                userAlertConsumerGroup
        );
    }

    @Bean(name = "userAlertKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserAlertEvent> userAlertKafkaListenerContainerFactory(
            ConsumerFactory<String, UserAlertEvent> userAlertEventConsumerFactory
    ) {
        return buildListenerFactory(userAlertEventConsumerFactory);
    }

    @Bean
    public ConsumerFactory<String, SmsDispatchCommand> smsDispatchCommandConsumerFactory(
            KafkaProperties kafkaProperties,
            SslBundles sslBundles
    ) {
        return KafkaConsumerFactorySupport.createConsumerFactory(
                kafkaProperties,
                sslBundles,
                SmsDispatchCommand.class,
                smsDispatchConsumerGroup
        );
    }

    @Bean(name = "smsDispatchKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, SmsDispatchCommand> smsDispatchKafkaListenerContainerFactory(
            ConsumerFactory<String, SmsDispatchCommand> smsDispatchCommandConsumerFactory
    ) {
        return buildListenerFactory(smsDispatchCommandConsumerFactory);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildListenerFactory(
            ConsumerFactory<String, T> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(CONCURRENCY);
        factory.setAutoStartup(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(buildErrorHandler());
        return factory;
    }

    private DefaultErrorHandler buildErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> log.warn(
                        "Recovered and skipped Kafka record. topic={}, partition={}, offset={}, key={}, exception={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.key(),
                        ex.getClass().getSimpleName()
                ),
                new FixedBackOff(RETRY_INTERVAL_MS, RETRY_MAX_ATTEMPTS)
        );

        // Deserialization poison messages should not loop forever.
        errorHandler.addNotRetryableExceptions(DeserializationException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}
