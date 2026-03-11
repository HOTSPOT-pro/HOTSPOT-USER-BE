package hotspot.user.usage.totalUsage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.totalUsage.controller.port.FindTotalUsageService;
import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;
import hotspot.user.usage.totalUsage.controller.swagger.TotalUsageApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/totalUsage")
public class TotalUsageController implements TotalUsageApi {

    private final FindTotalUsageService findTotalUsageService;

    @GetMapping
    public ResponseEntity<ApiResponse<TotalUsageResponse>> findTotalUsage(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                findTotalUsageService.findTotalUsage(
                        details.getId())));
    }
}
