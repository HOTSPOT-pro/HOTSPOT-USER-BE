package hotspot.user.family.domain.mapper;

import hotspot.user.family.controller.response.FamilyResponse;
import hotspot.user.family.domain.Family;

/**
 * request Dto -> 도메인
 * 도메인 -> response Dto
 */

public class FamilyMapper {
    // request -> domain

    // domain -> response
    public static FamilyResponse toFamilyResponse(Family family) {
        return FamilyResponse.from(family);
    }
}
