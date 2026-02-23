package hotspot.user.usage.subscriptionUsage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.controller.swagger.SubscriptionUsageApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptionUsage")
public class SubscriptionUsageController implements SubscriptionUsageApi {

    private final FindSubscriptionUsageService findSubscriptionUsageService;

    @GetMapping()
    public ResponseEntity<ApiResponse<SubscriptionUsageResponse>> findSubscriptionUsage(
            @AuthenticationPrincipal PrincipalDetails details) {
        return ResponseEntity.ok(ApiResponse.success(
                findSubscriptionUsageService.findSubscriptionUsage(details.getId())));
    }
}
