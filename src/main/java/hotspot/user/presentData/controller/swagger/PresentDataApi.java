package hotspot.user.presentData.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.FamilyDataResponse;
import hotspot.user.presentData.controller.response.PresentDataResponse;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "PresentData", description = "가족 데이터 조회 및 선물 데이터 조회 API")
public interface PresentDataApi {

    @Operation(
            summary = "가족 데이터 조회",
            description = "로그인 사용자가 속한 가족의 데이터 사용 현황을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            인증 실패
                            - AUTH_002: 유효하지 않은 토큰
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            조회 실패
                            - SUB_001: 회선 정보를 찾을 수 없습니다.
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<hotspot.user.common.ApiResponse<FamilyDataResponse>> findFamilyData(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(
            summary = "선물 받은 데이터 조회",
            description = "사용자가 받은 선물 데이터 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            인증 실패
                            - AUTH_002: 유효하지 않은 토큰
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            조회 실패
                            - SUB_001: 회선 정보를 찾을 수 없습니다.
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<hotspot.user.common.ApiResponse<PresentDataResponse>> findPresentReceive(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(
            summary = "선물 제공 데이터 조회",
            description = "사용자가 제공한 선물 데이터 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            인증 실패
                            - AUTH_002: 유효하지 않은 토큰
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            조회 실패
                            - SUB_001: 회선 정보를 찾을 수 없습니다.
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<hotspot.user.common.ApiResponse<PresentDataResponse>> findPresentProvide(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );

    @Operation(
            summary = "데이터 선물하기",
            description = "같은 가족 구성원에게 데이터를 선물합니다. (1GB ~ 5GB 단위)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선물하기 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            잘못된 요청
                            - COMMON_002: 입력값 유효성 검증 실패 (1~5GB 범위를 벗어남 등)
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            권한 없음
                            - AUTH_004: 동일 가족 구성원이 아님
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            조회 실패
                            - MEMBER_001: 회원 정보를 찾을 수 없음
                            - SUB_001: 회선 정보를 찾을 수 없음
                            """,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<hotspot.user.common.ApiResponse<SendPresentDataResponse>> sendPresentData(
            @Valid @RequestBody SendPresentDataRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails details
    );
}
