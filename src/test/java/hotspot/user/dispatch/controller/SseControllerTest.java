package hotspot.user.dispatch.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.dispatch.sse.controller.SseController;
import hotspot.user.dispatch.sse.controller.port.SubscribeSseService;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

@ExtendWith(MockitoExtension.class)
class SseControllerTest {

    @Mock
    private SubscribeSseService subscribeSseService;

    @InjectMocks
    private SseController dispatchSseController;

    @Test
    @DisplayName("subscribe delegates to sse stream service")
    void subscribeDelegatesToService() {
        PrincipalDetails details = PrincipalDetails.builder()
                .id(1L)
                .email("test@example.com")
                .familyId(10L)
                .role(FamilyRole.OWNER)
                .status(Status.APPROVED)
                .build();
        SseEmitter emitter = new SseEmitter();
        given(subscribeSseService.subscribe(1L, "10")).willReturn(emitter);

        SseEmitter response = dispatchSseController.subscribe(details, "10");

        assertThat(response).isSameAs(emitter);
        then(subscribeSseService).should().subscribe(1L, "10");
    }
}
