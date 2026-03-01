package hotspot.user.policy.controller;

import java.util.List;

import hotspot.user.policy.controller.port.DeleteFamilyBlockPolicyService;
import hotspot.user.policy.controller.port.FindBlockPolicyService;
import hotspot.user.policy.controller.port.FindFamilyBlockPolicyService;
import hotspot.user.policy.controller.port.FindSingleBlockPolicyService;
import hotspot.user.policy.controller.port.UpdateFamilyBlockPolicyStatusService;
import hotspot.user.policy.controller.request.BlockPolicyRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
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
    private final UpdateFamilyBlockPolicyStatusService updateFamilyBlockPolicyStatusService; // 우리 가족의 정책 상태 업데이트(비/활성화)
    private final DeleteFamilyBlockPolicyService deleteFamilyBlockPolicyService; // 우리 가족 정책 삭제
    private final FindSingleBlockPolicyService findSingleBlockPolicyService; // 단일 정책 조회

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getAllPolicies() {
        List<BlockPolicyResponse> policiesList = findBlockPolicyService.findAll();

        return ResponseEntity.ok()
                .body(ApiResponse.success(policiesList));
    }

    // 우리가족 정책 조회
    @Override
    @GetMapping("/families")
    public ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getFamilyPolicies(
            @AuthenticationPrincipal PrincipalDetails principal) {
        Long memberId = principal.getId();
        Long familyId = principal.getFamilyId();

        List<BlockPolicyResponse> policiesList = findFamilyBlockPolicyService.findAllByFamilyId(memberId, familyId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(policiesList));
    }

    // 가족 정책 isActive 업데이트
    @Override
    @PatchMapping("/families")
    public ResponseEntity<ApiResponse<UpdateFamilyBlockPolicyStatusResponse>> updateFamilyBlockPolicies(
            @RequestBody @Valid UpdateFamilyBlockPolicyStatusRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        Long memberId = principal.getId();
        Long familyId = principal.getFamilyId();

        UpdateFamilyBlockPolicyStatusResponse policiesList = updateFamilyBlockPolicyStatusService
                .updateFamilyBlockPolicyStatus(request, memberId, familyId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(policiesList));
    }

    // 우리 가족 정책 삭제 (단일, 일괄 모두 가능)
    @Override
    @DeleteMapping("/families")
    public ResponseEntity<ApiResponse<Void>> deleteFamilyBlockPolicies(
            @RequestParam List<Long> policyIdList,
            @AuthenticationPrincipal PrincipalDetails principal) {

        Long memberId = principal.getId();
        Long familyId = principal.getFamilyId();
        deleteFamilyBlockPolicyService.delete(policyIdList, memberId, familyId);

        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }

    // 정책 단일 조회
    @Override
    @GetMapping("/{blockPolicyId}")
    public ResponseEntity<ApiResponse<BlockPolicyResponse>> getBlockPolicy(
            @PathVariable Long blockPolicyId,
            @AuthenticationPrincipal PrincipalDetails principal) {

        BlockPolicyResponse response = findSingleBlockPolicyService
                .find(blockPolicyId, principal.getId(), principal.getFamilyId());

        return ResponseEntity.ok()
                .body(ApiResponse.success(response));
    }

    // 정책 생성
    @PostMapping
    public ResponseEntity<ApiResponse<BlockPolicyResponse>> createBlockPolicy(
            @RequestBody BlockPolicyRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }

    // 정책 수정
    @PatchMapping("/{blockPolicyId}")
    public ResponseEntity<ApiResponse<BlockPolicyResponse>> updateBlockPolicy(
            @PathVariable Long blockPolicyId,
            @RequestBody BlockPolicyRequest request,
            @AuthenticationPrincipal PrincipalDetails principal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }

}
