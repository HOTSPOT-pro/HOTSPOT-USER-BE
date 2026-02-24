package hotspot.user.dispatch.controller.swagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dispatch", description = "Notification SSE subscription API")
public interface SseApi {

    @Operation(
            summary = "Subscribe SSE stream",
            description = "Subscribe to notification SSE stream via /api/v1/sse/subscribe."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscribed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed (AUTH_002: invalid token)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription target not found (NOTI_001)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    SseEmitter subscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details,
            @Parameter(description = "Last SSE event id for reconnect", example = "10", required = false)
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    );
}
