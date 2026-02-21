package hotspot.user.policy.controller.request;

import java.util.List;

/**
 * 구성원별 차단 서비스 업데이트 request dto
 * @param familyId
 * @param subId
 * @param blockedServiceIdList
 */
public record UpdateAppBlockedServiceRequest(
        Long familyId,
        Long subId,
        List<Long> blockedServiceIdList
) {
}
