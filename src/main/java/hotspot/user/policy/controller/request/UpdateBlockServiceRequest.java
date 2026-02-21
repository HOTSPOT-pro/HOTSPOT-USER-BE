package hotspot.user.policy.controller.request;

import java.util.List;

/**
 * 구성원별 차단 서비스 업데이트 request dto
 * @param familyId
 * @param subId
 * @param blockedServiceStatusList
 */
public record UpdateBlockServiceRequest(
        Long familyId,
        Long subId,
        List<BlockedServiceStatus> blockedServiceStatusList
) {
}
