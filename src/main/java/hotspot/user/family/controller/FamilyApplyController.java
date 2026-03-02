package hotspot.user.family.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.port.AddFamilyMemberService;
import hotspot.user.family.controller.port.CreateNewFamilyService;
import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.family.controller.swagger.FamilyApplyApi;
import lombok.RequiredArgsConstructor;

/**
 * FamilySubscription 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/families")
public class FamilyApplyController implements FamilyApplyApi {

    private final CreateNewFamilyService createNewFamilyService; // 가족 신규 생성
    private final AddFamilyMemberService addFamilyMemberService; // 가족 구성원 추가 신청

    // 가족 신규 생성
    @Override
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateNewFamilyResponse>> createNewFamily(
            @Valid @RequestBody CreateNewFamilyRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        CreateNewFamilyResponse response = createNewFamilyService.createNewFamily(
                principal.getId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 가족 구성원 추가 신청 (다건)
    @Override
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<AddFamilyMemberResponse>> addFamilyMember(
            @Valid @RequestBody AddFamilyMemberRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        AddFamilyMemberResponse response = addFamilyMemberService.addFamilyMember(
                principal.getId(),
                principal.getFamilyId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
