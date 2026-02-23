package hotspot.user.usage.subscriptionUsage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.controller.swagger.SubscriptionUsageApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptionUsage")
public class SubscriptionUsageController implements SubscriptionUsageApi {

    private final FindSubscriptionUsageService findSubscriptionUsageService;

    @GetMapping("{subscriptionId}")
    public ResponseEntity<ApiResponse<SubscriptionUsageResponse>> findSubscriptionUsage(
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(ApiResponse.success(
                findSubscriptionUsageService.findSubscriptionUsage(subscriptionId)));
    }
}
