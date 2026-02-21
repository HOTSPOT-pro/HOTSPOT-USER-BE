package hotspot.user.policy.controller.swagger;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "App Blocking", description = "앱 차단 서비스 관련 API")
public interface AppBlockedServiceApi {

    @Operation(summary = "차단 가능한 앱 목록 조회", description = "관리자가 설정한 차단 가능한 전체 앱 서비스 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<List<AppBlockedServiceResponse>>> getAllBlockedApps();

    @Operation(summary = "구성원별 앱 차단 설정 업데이트",
               description = "가족 OWNER가 특정 구성원의 앱 차단 여부를 일괄 업데이트합니다. "
                           + "본인 가족 구성원만 수정 가능하며, 활성화할 ID 리스트를 전달합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- COMMON_002: 입력값 유효성 검증 실패",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: OWNER 권한이 아님\n"
                                         + "- FAMILY_003: 동일 가족 구성원이 아님",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- FAMILY_002: 해당 회선 정보를 찾을 수 없음",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<UpdateAppBlockedServiceResponse>> updateAppBlockedService(
            @Valid @RequestBody UpdateAppBlockedServiceRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails);
}
