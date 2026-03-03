package hotspot.user.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class LenientJsonOrStringDeserializer<T> implements Deserializer<T> {

    private final Class<T> targetClass;
    private final ObjectMapper objectMapper;

    public LenientJsonOrStringDeserializer(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return objectMapper.readValue(data, targetClass);
        } catch (IOException directParseFailure) {
            try {
                String wrappedJson = objectMapper.readValue(data, String.class);
                return objectMapper.readValue(wrappedJson, targetClass);
            } catch (IOException wrappedParseFailure) {
                String raw = new String(data, StandardCharsets.UTF_8);
                throw new SerializationException(
                        "Failed to deserialize topic " + topic + " into " + targetClass.getName()
                                + ". Raw payload: " + raw,
                        wrappedParseFailure
                );
            }
        }
    }
}
