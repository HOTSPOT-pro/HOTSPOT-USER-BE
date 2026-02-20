package hotspot.user.policy.controller.response;

import lombok.Builder;

/**
 * 관리자 앱 차단 서비스 응답 dto
 */
@Builder
public record AppBlockedServiceResponse(
        Long id,
        String name,
        String serviceCode // 카테고리 표시 때문에 필요할 듯
) {
}
