package hotspot.user.policy.domain.mapper;

import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.BlockedServiceSub;

import java.util.List;

/**
 * request -> 도메인
 * 도메인 -> response
 */
public class AppBlockedServiceMapper {
    // request -> domain

    // domain -> response
    // AppBlockedService(앱 차단 서비스 원본) 도메인을 AppBlockedServiceResponse DTO로 변환
    public static AppBlockedServiceResponse toAppBlockedServiceResponse(AppBlockedService appBlockedService) {
        return AppBlockedServiceResponse.builder()
                .id(appBlockedService.getId())
                .name(appBlockedService.getName())
                .serviceCode(appBlockedService.getServiceCode())
                .build();
    }

    // BlockedServiceSub(적용된 앱 차단) 도메인을 AppBlockedServiceResponse DTO로 변환
    public static AppBlockedServiceResponse toAppBlockedServiceResponse(BlockedServiceSub blockedSub) {
        return AppBlockedServiceResponse.builder()
                .id(blockedSub.getAppBlockedService().getId())
                .name(blockedSub.getAppBlockedService().getName())
                .serviceCode(blockedSub.getAppBlockedService().getServiceCode())
                .build();
    }

    // 앱 차단 서비스 업데이트 response dto로 변환
    public static UpdateAppBlockedServiceResponse toUpdateAppBlockedServiceResponse(
            Long familyId, Long subId, List<Long> blockedServiceIdList) {
        return UpdateAppBlockedServiceResponse.builder()
                .familyId(familyId)
                .subId(subId)
                .blockedServiceIdList(blockedServiceIdList)
                .build();
    }
}
