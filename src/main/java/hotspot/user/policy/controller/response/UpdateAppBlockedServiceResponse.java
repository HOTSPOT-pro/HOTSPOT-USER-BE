package hotspot.user.policy.controller.response;

import lombok.Builder;

import java.util.List;

/**
 * 구성원별 차단 서비스 업데이트 response dto
 * @param familyId
 * @param subId
 * @param blockedServiceIdList
 */

@Builder
public record UpdateAppBlockedServiceResponse(
        Long familyId,
        Long subId,
        List<Long> blockedServiceIdList
) {
}
