package hotspot.user.presentData.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.presentData.controller.response.FamilyDataResponse;
import hotspot.user.presentData.controller.response.PresentDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "PresentData", description = "가족 데이터 조회 및 선물 데이터 조회 API")
public interface PresentDataApi {

    @Operation(
            summary = "가족 데이터 조회",
            description = "로그인 사용자가 속한 가족의 데이터 사용 현황을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패\n- AUTH_002: 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "가족 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<FamilyDataResponse>> findFamilyData(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(
            summary = "선물 받은 데이터 조회",
            description = "사용자가 받은 선물 데이터 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패\n- AUTH_002: 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 실패\n- SUB_001: 회선 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<PresentDataResponse>> findPresentReceive(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(
            summary = "선물 제공 데이터 조회",
            description = "사용자가 제공한 선물 데이터 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패\n- AUTH_002: 유효하지 않은 토큰",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 실패\n- SUB_001: 회선 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<PresentDataResponse>> findPresentProvide(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );
}
