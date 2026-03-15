package hotspot.user.familyReport.controller;

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
import hotspot.user.familyReport.controller.port.CreateFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.FindFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import hotspot.user.familyReport.controller.swagger.FamilyReportApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-reports")
public class FamilyReportController implements FamilyReportApi {

    private final CreateFamilyReportSubscriptionService createFamilyReportSubscriptionService;
    private final FindFamilyReportSubscriptionService findFamilyReportSubscriptionService;

    @Override
    @GetMapping("/families")
    public ResponseEntity<ApiResponse<FamilyReportSubscriptionResponse>> findFamilyReportSubscription(
            @AuthenticationPrincipal PrincipalDetails principal) {

        FamilyReportSubscriptionResponse response =
                findFamilyReportSubscriptionService.findSubscription(principal.getFamilyId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PostMapping("/families")
    public ResponseEntity<ApiResponse<Void>> createFamilyReportSubscription(
            @Valid @RequestBody CreateFamilyReportSubscriptionRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        createFamilyReportSubscriptionService.createSubscription(
                principal.getFamilyId(),
                principal.getRole(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success());
    }
}
