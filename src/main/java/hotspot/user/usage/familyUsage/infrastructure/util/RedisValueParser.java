package hotspot.user.usage.familyUsage.infrastructure.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RedisValueParser {

    public static double toDouble(Object value) {
        if (value == null) {
            return 0D;
        }

        if (value instanceof byte[] bytes) {
            return Double.parseDouble(new String(bytes, StandardCharsets.UTF_8));
        }

        return Double.parseDouble(value.toString());
    }
}
