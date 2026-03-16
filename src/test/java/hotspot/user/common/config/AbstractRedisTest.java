package hotspot.user.common.config;


import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataRedisTest
@ActiveProfiles("test")
@Import(RedisContainerTestConfig.class)
@ContextConfiguration(classes = AbstractRedisTest.TestBootConfig.class)
public abstract class AbstractRedisTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestBootConfig {
    }
}
