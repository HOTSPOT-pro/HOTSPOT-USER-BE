package hotspot.user.presentData.controller;

 import hotspot.user.common.ApiResponse;
 import hotspot.user.common.security.PrincipalDetails;
 import hotspot.user.presentData.controller.port.FindFamilyDataService;
 import hotspot.user.presentData.controller.response.FamilyDataResponse;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presentData")
public class PresentDataController {

    private final FindFamilyDataService findFamilyDataService;

    @GetMapping
    public ResponseEntity<ApiResponse<FamilyDataResponse>> findFamilyData(
            @AuthenticationPrincipal PrincipalDetails details) {
        return ResponseEntity.ok(ApiResponse.success(
                findFamilyDataService.findFamilyData(
                        details.getId(), details.getFamilyId())));
    }
}
