package hotspot.user.dispatch.controller.port;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SubscribeSseService {

    SseEmitter subscribe(Long memberId, String lastEventId);
}
