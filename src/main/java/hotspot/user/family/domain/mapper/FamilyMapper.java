package hotspot.user.family.domain.mapper;

import java.util.List;

import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.controller.response.FamilyMemberInfoResponse;
import hotspot.user.family.controller.response.FamilyResponse;
import hotspot.user.family.controller.response.MemberPriorityResponse;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.domain.FamilySubscription;

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

    public static UpdateFamilyPriorityResponse toUpdateFamilyPriorityResponse(
            Family family,
            List<FamilySubscription> familySubList) {
        return UpdateFamilyPriorityResponse.builder()
                .familyId(family.getId())
                .priorityType(family.getPriorityType())
                .memberPriorities(toMemberPriorities(familySubList))
                .build();
    }

    // List 변환 전용 private 메서드
    private static List<MemberPriorityResponse> toMemberPriorities(
            List<FamilySubscription> familySubList) {

        return familySubList.stream()
                .map(sub -> MemberPriorityResponse.builder()
                        .subId(sub.getSubscription().getId())
                        .priority(sub.getPriority())
                        .build())
                .toList();
    }

    public static FamilyInfoResponse toFamilyInfoResponse(
            FamilyDetailInfo detailInfo,
            List<FamilyMemberInfoResponse> memberInfoList) {
        return FamilyInfoResponse.builder()
                .familyId(detailInfo.getFamilyId())
                .familyNum(detailInfo.getFamilyNum())
                .memberInfoList(memberInfoList)
                .build();
    }
}
