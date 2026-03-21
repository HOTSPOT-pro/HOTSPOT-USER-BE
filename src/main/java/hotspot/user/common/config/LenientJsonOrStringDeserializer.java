package hotspot.user.common.config;

import java.io.IOException;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class LenientJsonOrStringDeserializer<T> implements Deserializer<T> {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private final Class<T> targetClass;

    public LenientJsonOrStringDeserializer(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(data, targetClass);
        } catch (IOException directParseFailure) {
            try {
                String wrappedJson = OBJECT_MAPPER.readValue(data, String.class);
                return OBJECT_MAPPER.readValue(wrappedJson, targetClass);
            } catch (IOException wrappedParseFailure) {
                throw new SerializationException(
                        "Failed to deserialize topic " + topic + " into " + targetClass.getName(),
                        wrappedParseFailure
                );
            }
        }
    }
}
