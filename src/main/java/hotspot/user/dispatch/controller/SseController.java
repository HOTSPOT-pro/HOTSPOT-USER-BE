package hotspot.user.dispatch.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.dispatch.controller.port.SubscribeSseService;
import hotspot.user.dispatch.controller.swagger.SseApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sse")
public class SseController implements SseApi {

    private final SubscribeSseService subscribeSseService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    // 인증된 사용자의 SSE 구독을 시작하고, 마지막 이벤트 ID를 전달해 스트림을 연결한다.
    public SseEmitter subscribe(
            @AuthenticationPrincipal PrincipalDetails details,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    ) {
        return subscribeSseService.subscribe(details.getId(), lastEventId);
    }
}
