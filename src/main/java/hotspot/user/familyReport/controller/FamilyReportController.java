package hotspot.user.familyReport.controller;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.familyReport.controller.port.CancelFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.CreateFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.FindFamilyReportMembersService;
import hotspot.user.familyReport.controller.port.FindFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.UpdateFamilyReportReceiveDayService;
import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.familyReport.controller.request.UpdateFamilyReportReceiveDayRequest;
import hotspot.user.familyReport.controller.response.FamilyReportMemberResponse;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import hotspot.user.familyReport.controller.swagger.FamilyReportApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-reports")
public class FamilyReportController implements FamilyReportApi {

    private final CancelFamilyReportSubscriptionService cancelFamilyReportSubscriptionService;
    private final CreateFamilyReportSubscriptionService createFamilyReportSubscriptionService;
    private final FindFamilyReportMembersService findFamilyReportMembersService;
    private final FindFamilyReportSubscriptionService findFamilyReportSubscriptionService;
    private final UpdateFamilyReportReceiveDayService updateFamilyReportReceiveDayService;

    @Override
    @GetMapping("/families")
    public ResponseEntity<ApiResponse<FamilyReportSubscriptionResponse>> findFamilyReportSubscription(
            @AuthenticationPrincipal PrincipalDetails principal) {

        FamilyReportSubscriptionResponse response =
                findFamilyReportSubscriptionService.findSubscription(principal.getFamilyId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping("/families/members")
    public ResponseEntity<ApiResponse<List<FamilyReportMemberResponse>>> findFamilyReportMembers(
            @AuthenticationPrincipal PrincipalDetails principal) {

        List<FamilyReportMemberResponse> response =
                findFamilyReportMembersService.findMembers(principal.getFamilyId());

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

    @Override
    @PatchMapping("/families/receive-day")
    public ResponseEntity<ApiResponse<Void>> updateFamilyReportReceiveDay(
            @Valid @RequestBody UpdateFamilyReportReceiveDayRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        updateFamilyReportReceiveDayService.updateReceiveDay(
                principal.getFamilyId(),
                principal.getRole(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Override
    @DeleteMapping("/families")
    public ResponseEntity<ApiResponse<Void>> cancelFamilyReportSubscription(
            @AuthenticationPrincipal PrincipalDetails principal) {

        cancelFamilyReportSubscriptionService.cancelSubscription(
                principal.getFamilyId(),
                principal.getRole()
        );

        return ResponseEntity.ok(ApiResponse.success());
    }
}
