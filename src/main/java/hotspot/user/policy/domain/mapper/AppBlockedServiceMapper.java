package hotspot.user.policy.domain.mapper;

import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.domain.AppBlockedService;

/**
 * request -> 도메인
 * 도메인 -> response
 */
public class AppBlockedServiceMapper {
    // request -> domain

    // domain -> response
    public static AppBlockedServiceResponse toAppBlockedServiceResponse(AppBlockedService appBlockedService) {
        return AppBlockedServiceResponse.from(appBlockedService);
    }
}
