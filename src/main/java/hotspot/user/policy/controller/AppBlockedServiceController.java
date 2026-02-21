package hotspot.user.policy.controller;

import java.util.List;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.policy.controller.port.FindAppBlockedService;
import hotspot.user.policy.controller.port.UpdateAppBlockedServiceService;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 앱 차단 서비스 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blocking")
public class AppBlockedServiceController {

    private final FindAppBlockedService findAppBlockedService;
    private final UpdateAppBlockedServiceService updateAppBlockedServiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppBlockedServiceResponse>>> getAllBlockedApps() {
        List<AppBlockedServiceResponse> blockedAppList = findAppBlockedService.findAll();

        return ResponseEntity.ok()
                .body(ApiResponse.success(blockedAppList));
    }

    // 구성원별 앱 차단 설정 업데이트
    @PatchMapping
    public ResponseEntity<ApiResponse<UpdateAppBlockedServiceResponse>> updateAppBlockedService(
            @Valid @RequestBody UpdateAppBlockedServiceRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        UpdateAppBlockedServiceResponse response = updateAppBlockedServiceService.updateAppBlockedService(
                request,
                principalDetails.getFamilyId(),
                principalDetails.getRole()
        );

        return ResponseEntity.ok()
                .body(ApiResponse.success(response));
    }
}
