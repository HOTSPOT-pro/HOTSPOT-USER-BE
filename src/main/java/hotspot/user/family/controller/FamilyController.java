package hotspot.user.family.controller;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.port.FindFamilyInfoService;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.controller.swagger.FamilyApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Family 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/families/")
public class FamilyController implements FamilyApi {

    private final FindFamilyInfoService findFamilyInfoService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<FamilyInfoResponse>> getFamilyInfo(
            @AuthenticationPrincipal PrincipalDetails principal) {

        FamilyInfoResponse response = findFamilyInfoService.findFamilyInfoById(principal.getFamilyId());
        return ResponseEntity.ok(
                ApiResponse.success(response));

    }
}
