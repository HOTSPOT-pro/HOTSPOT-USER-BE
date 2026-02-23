package hotspot.user.family.domain.mapper;

import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 구성원 추가 / 삭제 신청 Mapper
 */
public class FamilyApplyMapper {
    // request -> 도메인
    public static FamilyApply toFamilyApply(Long requesterSubId,
                                            Long familyId,
                                            CreateFamilyApplyRequest createFamilyApplyRequest) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .targetSubId(createFamilyApplyRequest.targetSubId())
                .familyId(familyId)
                .applyType(createFamilyApplyRequest.applyType())
                .targetFamilyRole(createFamilyApplyRequest.targetFamilyRole())
                .docUrl(createFamilyApplyRequest.docUrl())
                .status(ApplyStatus.PENDING)
                .build();

    }

    // 도메인 -> response
    public static CreateFamilyApplyResponse toCreateFamilyApplyResponse(FamilyApply familyApply) {
        return CreateFamilyApplyResponse.builder()
                .requesterSubId(familyApply.getRequesterSubId())
                .targetSubId(familyApply.getTargetSubId())
                .familyId(familyApply.getFamilyId())
                .applyType(familyApply.getApplyType())
                .targetFamilyRole(familyApply.getTargetFamilyRole())
                .docUrl(familyApply.getDocUrl())
                .status(familyApply.getStatus())
                .build();
    }
}
