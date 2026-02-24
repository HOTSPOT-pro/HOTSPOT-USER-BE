package hotspot.user.presentData.controller;

 import hotspot.user.common.ApiResponse;
 import hotspot.user.common.security.PrincipalDetails;
 import hotspot.user.presentData.controller.port.FindFamilyDataService;
 import hotspot.user.presentData.controller.port.FindPresentReceiveService;
 import hotspot.user.presentData.controller.port.FindPresentProvideService;
 import hotspot.user.presentData.controller.response.FamilyDataResponse;
 import hotspot.user.presentData.controller.response.PresentDataResponse;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.core.annotation.AuthenticationPrincipal;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presentData")
public class PresentDataController {

    private final FindFamilyDataService findFamilyDataService;
    private final FindPresentReceiveService findPresentReceiveService;
    private final FindPresentProvideService findPresentProvideService;
    @GetMapping
    public ResponseEntity<ApiResponse<FamilyDataResponse>> findFamilyData(
            @AuthenticationPrincipal PrincipalDetails details) {
        return ResponseEntity.ok(ApiResponse.success(
                findFamilyDataService.findFamilyData(
                        details.getId(), details.getFamilyId())));
    }

    @GetMapping("/receive/{memberId}")
    public ResponseEntity<ApiResponse<PresentDataResponse>> findPresentReceive(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                findPresentReceiveService.findPresentReceive(memberId)));
    }

    @GetMapping("/provide/{memberId}")
    public ResponseEntity<ApiResponse<PresentDataResponse>> findPresentProvide(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                findPresentProvideService.findPresentProvide(memberId)));
    }
}
