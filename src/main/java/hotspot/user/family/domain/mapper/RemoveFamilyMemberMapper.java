package hotspot.user.family.domain.mapper;

import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.FamilyApply;

/**
 * 가족 구성원 삭제 신청 Mapper
 */
public class RemoveFamilyMemberMapper {

    // requeset -> domain
    public static FamilyApply toFamilyApply() {
        return FamilyApply.builder()
                .build();
    }

    // domain -> response
    public static RemoveFamilyMemberResponse toRemoveFamilyMemberResponse() {

    }
}
