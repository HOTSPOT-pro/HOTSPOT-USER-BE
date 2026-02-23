package hotspot.user.family.controller;

import hotspot.user.family.controller.port.UpdateFamilyRoleService;
import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.member.domain.FamilyRole;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.port.UpdateDataLimitService;
import hotspot.user.family.controller.port.UpdateFamilyPriorityService;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
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
    private final UpdateFamilyPriorityService updateFamilyPriorityService;
    private final UpdateFamilyRoleService updateFamilyRoleService; // 가족 역할 업데이트 서비스

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

    // 가족 우선순위 정책 및 구성원 순위 업데이트
    @Override
    @PatchMapping("/priority")
    public ResponseEntity<ApiResponse<UpdateFamilyPriorityResponse>> updateFamilyPriority(
            @Valid @RequestBody UpdateFamilyPriorityRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        UpdateFamilyPriorityResponse response = updateFamilyPriorityService.updateFamilyPriority(
                request,
                principal.getFamilyId(),
                principal.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 구성원의 역할 (PARENT <-> CHILD) 업데이트
    @Override
    @PatchMapping("/members/{subId}/role")
    public ResponseEntity<ApiResponse<UpdateFamilyRoleResponse>> updateFamilyRole(
            @PathVariable Long subId,
            @Valid @RequestBody UpdateFamilyRoleRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        UpdateFamilyRoleResponse response = updateFamilyRoleService.update(
                principal.getId(),
                principal.getFamilyId(),
                principal.getRole(),
                subId,
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
