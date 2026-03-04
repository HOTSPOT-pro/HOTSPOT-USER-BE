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
import org.springframework.util.backoff.ExponentialBackOff;

import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.kafka.dto.UserAlertEvent;

@Configuration
public class KafkaConsumerConfig {

    private static final int CONCURRENCY = 6;
    private static final long BACKOFF_INITIAL_INTERVAL_MS = 500L;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long BACKOFF_MAX_INTERVAL_MS = 10_000L;
    private static final long BACKOFF_MAX_ELAPSED_MS = 60_000L;

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
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        ExponentialBackOff backOff = new ExponentialBackOff(BACKOFF_INITIAL_INTERVAL_MS, BACKOFF_MULTIPLIER);
        backOff.setMaxInterval(BACKOFF_MAX_INTERVAL_MS);
        backOff.setMaxElapsedTime(BACKOFF_MAX_ELAPSED_MS);

        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));
        return factory;
    }
}
