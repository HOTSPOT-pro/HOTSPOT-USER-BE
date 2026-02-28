package hotspot.user.policy.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.port.UpdatePolicySubService;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import hotspot.user.policy.controller.swagger.AppliedPolicyApi;
import lombok.RequiredArgsConstructor;

/**
 * 적용된 정책(시간 정책 및 앱 차단) 조회 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policies")
public class AppliedPolicyController implements AppliedPolicyApi {

    private final FindMemberAppliedPolicyService findMemberAppliedPolicyService; // 구성원별 적용 정책 조회
    private final FindFamilyAppliedPolicyService findFamilyAppliedPolicyService; // 가족 구성원 전체 적용 정책 조회
    private final UpdatePolicySubService updatePolicySubService; // 구성원 별 정책 업데이트 (적용)

    /**
     * 적용된 정책 목록을 조회 API
     * @param isFamily true일 경우 가족 전체의 정책을, false일 경우 본인의 정책만 조회
     */
    @Override
    @GetMapping("/applied")
    public ResponseEntity<ApiResponse<Object>> getAppliedPolicies(
            @RequestParam(defaultValue = "false") boolean isFamily,
            @AuthenticationPrincipal PrincipalDetails principal
    ) {
        if (isFamily) {
            // 가족 전체 조회 시 권한 검증: OWNER 또는 PARENT만 가능
            if (principal.getRole() != FamilyRole.OWNER && principal.getRole() != FamilyRole.PARENT) {
                throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
            }

            // 토큰에 저장된 familyId를 사용하여 조회
            return ResponseEntity.ok(ApiResponse.success(
                findFamilyAppliedPolicyService.findByFamilyId(principal.getFamilyId())
            ));
        }

        // 본인 정책 조회
        return ResponseEntity.ok(ApiResponse.success(
            findMemberAppliedPolicyService.findByMemberId(principal.getId())
        ));
    }


    // 구성원별 앱 차단 설정 업데이트
    @Override
    @PutMapping("/apply")
    public ResponseEntity<ApiResponse<UpdatePolicySubResponse>> updatePolicySub(
            @Valid @RequestBody UpdatePolicySubRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
            ) {

        UpdatePolicySubResponse response = updatePolicySubService.updatePolicySub(
                request,
                principalDetails.getFamilyId(),
                principalDetails.getRole()
        );

        return ResponseEntity.ok()
                .body(ApiResponse.success(response));
    }
}
