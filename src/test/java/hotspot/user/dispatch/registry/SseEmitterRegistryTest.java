package hotspot.user.dispatch.registry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRegistryTest {

    private final SseEmitterRegistry sseEmitterRegistry = new SseEmitterRegistry();

    @Test
    @DisplayName("register indexes emitter by subId and remove cleans registry")
    void registerAndRemoveEmitter() {
        SseEmitter emitter = new SseEmitter();

        String emitterId = sseEmitterRegistry.register(11L, emitter);

        assertThat(sseEmitterRegistry.findBySubId(11L)).hasSize(1);
        assertThat(sseEmitterRegistry.findAll()).hasSize(1);

        sseEmitterRegistry.remove(emitterId);

        assertThat(sseEmitterRegistry.findBySubId(11L)).isEmpty();
        assertThat(sseEmitterRegistry.findAll()).isEmpty();
    }
}
