package hotspot.user.usage.familyUsage.controller;

import hotspot.user.common.ApiResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.usage.familyUsage.controller.swagger.FamilyUsageApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.familyUsage.controller.port.FindFamilyUsageService;
import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/familyUsage")
public class FamilyUsageController implements FamilyUsageApi {

    private final FindFamilyUsageService findFamilyUsageService;

    @GetMapping
    public ResponseEntity<ApiResponse<FamilyUsageResponse>> findFamilyUsage(
            @AuthenticationPrincipal PrincipalDetails details) {
        return ResponseEntity.ok(ApiResponse.success(
                findFamilyUsageService.findFamilyUsage(details.getFamilyId())));
    }
}
