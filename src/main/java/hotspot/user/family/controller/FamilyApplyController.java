package hotspot.user.family.controller;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.port.CreateFamilyApplyService;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.controller.swagger.FamilyApplyApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * FamilySubscription 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/families")
public class FamilyApplyController implements FamilyApplyApi {

    private final CreateFamilyApplyService createFamilyApplyService; // 가족 구성원 추가 / 삭제 신청 서비스

    // 가족 구성원 추가 / 삭제 신청
    @PostMapping
    public ResponseEntity<ApiResponse<CreateFamilyApplyResponse>> manageFamilyMember(
            @Valid @RequestBody CreateFamilyApplyRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        CreateFamilyApplyResponse response = createFamilyApplyService.manage(
                principal.getId(),
                principal.getFamilyId(),
                principal.getRole(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response));

    }
}
