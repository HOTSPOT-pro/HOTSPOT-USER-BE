package hotspot.user.policy.controller;

import java.util.List;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.policy.controller.port.FindFamilyBlockPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.policy.controller.port.FindBlockPolicyService;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.swagger.BlockPolicyApi;
import lombok.RequiredArgsConstructor;

/**
 * 정책 관련 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policies")
public class BlockPolicyController implements BlockPolicyApi {

    private final FindBlockPolicyService findBlockPolicyService;
    private final FindFamilyBlockPolicyService findFamilyBlockPolicyService; // 우리 가족이 생성한 정책 조회

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getAllPolicies() {
        List<BlockPolicyResponse> policiesList = findBlockPolicyService.findAll();

        return ResponseEntity.ok()
                .body(ApiResponse.success(policiesList));
    }

    // 우리가족 정책 조회
    @GetMapping("/families")
    public ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getFamilyPolicies(
            @AuthenticationPrincipal PrincipalDetails principal) {
        Long memberId = principal.getId();
        Long familyId = principal.getFamilyId();

        List<BlockPolicyResponse> policiesList = findFamilyBlockPolicyService.findAllByFamilyId(memberId, familyId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(policiesList));
    }

}
