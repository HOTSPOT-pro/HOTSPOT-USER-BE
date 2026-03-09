package hotspot.user.policy.controller.response;

import lombok.Builder;

/**
 * 차단된 정책 정보 (id, 이름)
 * @param id
 * @param name
 */
@Builder
public record BlockedReasonResponse(
        Long id,
        String name
) { }
