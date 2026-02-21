package hotspot.user.policy.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * 구성원별 차단 서비스 업데이트 request dto
 * @param familyId
 * @param subId
 * @param blockedServiceIdList
 */
public record UpdateAppBlockedServiceRequest(
        @NotNull Long familyId,
        @NotNull Long subId,
        @NotNull List<Long> blockedServiceIdList
) {
}
