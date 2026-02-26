package hotspot.user.presentData.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.presentData.controller.port.FindFamilyDataService;
import hotspot.user.presentData.controller.port.FindPresentProvideService;
import hotspot.user.presentData.controller.port.FindPresentReceiveService;
import hotspot.user.presentData.controller.port.SendPresentDataService;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.FamilyDataResponse;
import hotspot.user.presentData.controller.response.PresentDataResponse;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.controller.swagger.PresentDataApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presentData")
public class PresentDataController implements PresentDataApi {

    private final FindFamilyDataService findFamilyDataService;
    private final FindPresentReceiveService findPresentReceiveService;
    private final FindPresentProvideService findPresentProvideService;
    private final SendPresentDataService sendPresentDataService; // 데이터 선물하기 서비스

    @GetMapping
    public ResponseEntity<ApiResponse<FamilyDataResponse>> findFamilyData(
            @AuthenticationPrincipal PrincipalDetails details) {
        return ResponseEntity.ok(ApiResponse.success(
                findFamilyDataService.findFamilyData(
                        details.getId(), details.getFamilyId())));
    }

    @GetMapping("/receive")
    public ResponseEntity<ApiResponse<PresentDataResponse>> findPresentReceive(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                findPresentReceiveService.findPresentReceive(details.getId())));
    }

    @GetMapping("/provide")
    public ResponseEntity<ApiResponse<PresentDataResponse>> findPresentProvide(
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                findPresentProvideService.findPresentProvide(details.getId())));
    }

    // 데이터 선물하기
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<SendPresentDataResponse>> sendPresentData(
            @Valid @RequestBody SendPresentDataRequest request,
            @AuthenticationPrincipal PrincipalDetails details
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                sendPresentDataService.sendPresentData(details.getId(), request)
        ));
    }
}
