package hotspot.user.policy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.policy.controller.port.FindAppBlockedService;
import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import lombok.RequiredArgsConstructor;

/**
 * 앱 차단 서비스 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blocking")
public class AppBlockedServiceController {

    private final FindAppBlockedService findAppBlockedService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppBlockedServiceResponse>>> getAllBlockedApps() {
        List<AppBlockedServiceResponse> blockedAppList = findAppBlockedService.findAll();

        return ResponseEntity.ok()
                .body(ApiResponse.success(blockedAppList));
    }
}
