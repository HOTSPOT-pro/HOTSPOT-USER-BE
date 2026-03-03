package hotspot.user.common.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

public class KafkaConsumerFactorySupport {

    public static <T> ConsumerFactory<String, T> createConsumerFactory(
            KafkaProperties props,
            SslBundles sslBundles,
            Class<T> clazz,
            String groupId
    ) {
        Map<String, Object> cfg = new HashMap<>(props.buildConsumerProperties(sslBundles));
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(
                cfg,
                new StringDeserializer(),
                new LenientJsonOrStringDeserializer<>(clazz)
        );
    }

    private KafkaConsumerFactorySupport() {
    }
}
