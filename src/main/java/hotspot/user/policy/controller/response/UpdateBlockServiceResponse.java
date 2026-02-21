package hotspot.user.policy.controller.response;

import java.util.List;
import java.util.Set;

/**
 * 구성원별 차단 서비스 업데이트 response dto
 * @param familyId
 * @param subId
 * @param blockedServiceIdList
 */
public record UpdateBlockServiceResponse(
        Long familyId,
        Long subId,
        List<Long> blockedServiceIdList
) {
}
