package hotspot.user.family.domain.mapper;

import java.util.List;

import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;

/**
 * 가족 구성원 삭제 신청 Mapper
 */
public class RemoveFamilyMemberMapper {

    // request -> domain
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId, RemoveFamilyMemberRequest request) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(ApplyType.REMOVE)
                .status(ApplyStatus.PENDING)
                .build();
    }

    // domain -> response
    public static RemoveFamilyMemberResponse toRemoveFamilyMemberResponse(
            FamilyApply familyApply,
            List<FamilyApplyTarget> targets) {

        List<Long> subIdList = targets.stream()
                .map(FamilyApplyTarget::getTargetSubId)
                .toList();

        return RemoveFamilyMemberResponse.builder()
                .familyApplyId(familyApply.getId())
                .familyId(familyApply.getFamilyId())
                .subIdList(subIdList)
                .build();
    }
}
