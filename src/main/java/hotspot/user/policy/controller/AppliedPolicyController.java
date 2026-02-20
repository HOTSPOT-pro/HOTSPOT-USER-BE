package hotspot.user.policy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
import lombok.RequiredArgsConstructor;

/**
 * 적용된 정책(시간 정책 및 앱 차단) 조회 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policies/applied")
public class AppliedPolicyController {

    private final FindMemberAppliedPolicyService findMemberAppliedPolicyService; // 구성원별 적용 정책 조회
    private final FindFamilyAppliedPolicyService findFamilyAppliedPolicyService; // 가족 구성원 전체 적용 정책 조회

    /**
     * 적용된 정책 목록을 조회 Test API
     * @param isFamily true일 경우 가족 전체의 정책을, false일 경우 본인의 정책만 조회
     * 우리 정보로 만들어진 더미 데이터가 없기 때문에 파라미터로 memberId, familyId 전달할 수 있도록 한다.
     * [To-Do] 더미 데이터 및 로그인 기능 최종 완료 시 삭제 예정
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<Object>> getAppliedPoliciesTest(
            @RequestParam(defaultValue = "false") boolean isFamily,
            @RequestParam(required = false) Long testMemberId,
            @RequestParam(required = false) Long testFamilyId
    ) {
        if (isFamily) {
            return ResponseEntity.ok(ApiResponse.success(
                    findFamilyAppliedPolicyService.findByFamilyId(testFamilyId)
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                findMemberAppliedPolicyService.findByMemberId(testMemberId)
        ));
    }

    /**
     * 적용된 정책 목록을 조회 API
     * @param isFamily true일 경우 가족 전체의 정책을, false일 경우 본인의 정책만 조회
     */
    @GetMapping
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
}
