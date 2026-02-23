package hotspot.user.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.member.controller.port.FindMemberService;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.controller.swagger.MemberApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements MemberApi {

    private final FindMemberService findMemberService;

    // 내 정보 조회
    // [To-Do] 구성원 별 정보 조회는 전체 가족 조회할 때 모두 포함되는 값이라서 따로 API로 만들진 않았는데 추후 필요하면 추가하기
    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberInfo(
            @AuthenticationPrincipal PrincipalDetails principal) {

        MemberResponse result = findMemberService.findByIdAndEmail(principal.getId(), principal.getEmail());

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
