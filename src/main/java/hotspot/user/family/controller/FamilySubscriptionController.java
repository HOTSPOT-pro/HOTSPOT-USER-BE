package hotspot.user.family.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.port.UpdateDataLimitService;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.controller.swagger.FamilySubscriptionApi;
import lombok.RequiredArgsConstructor;

/**
 * FamilySubscription 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/families")
public class FamilySubscriptionController implements FamilySubscriptionApi {

    private final UpdateDataLimitService updateDataLimitService;

    // 구성원의 가족 공유 데이터 한도 조정
    @Override
    @PatchMapping("/data-limit")
    public ResponseEntity<ApiResponse<UpdateDataLimitResponse>> updateDataLimit(
            @Valid @RequestBody UpdateDataLimitRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        UpdateDataLimitResponse response = updateDataLimitService.updateDataLimit(
                request,
                principal.getFamilyId(),
                principal.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
