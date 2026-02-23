package hotspot.user.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisLuaConfig {

    @Bean
    public DefaultRedisScript<List> usageSumScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource("lua/usage_sum.lua")
        );

        script.setResultType(List.class);

        return script;
    }
}
