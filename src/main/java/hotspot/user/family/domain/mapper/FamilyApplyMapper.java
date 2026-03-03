package hotspot.user.family.domain.mapper;

import java.util.List;
import java.util.Map;

import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.family.controller.response.FamilyMemberResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;

/**
 * 가족 구성원 추가 / 삭제 신청 Mapper
 */
public class FamilyApplyMapper {

    // 1. 단건 신청용 (기존 CreateNewFamilyRequest 대응)
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId, CreateNewFamilyRequest request) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(request.applyType())
                .docUrl(request.docUrl())
                .status(ApplyStatus.PENDING)
                .build();
    }

    // 2. 다건 신청용 (AddFamilyMemberRequest 대응)
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId, AddFamilyMemberRequest request) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(request.applyType())
                .docUrl(request.docUrl())
                .status(ApplyStatus.PENDING)
                .build();
    }

    // 3. 삭제 신청용 (RemoveFamilyMemberRequest 대응)
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId, RemoveFamilyMemberRequest request) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(ApplyType.REMOVE)
                .status(ApplyStatus.PENDING)
                .build();
    }

    // 4. 타겟 도메인 생성
    public static FamilyApplyTarget toFamilyApplyTarget(Long familyApplyId, Long targetSubId, FamilyRole role) {
        return FamilyApplyTarget.builder()
                .familyApplyId(familyApplyId)
                .targetSubId(targetSubId)
                .targetFamilyRole(role)
                .build();
    }

    /**
     * 신규 가족 생성 응답 변환 (CreateNewFamilyResponse 전용)
     */
    public static CreateNewFamilyResponse toCreateNewFamilyResponse(
            Long familyId,
            ApplyType applyType,
            List<FamilyApplyTarget> targets,
            Map<Long, Subscription> subscriptionMap,
            Map<Long, String> subIdToPhoneMap) {

        List<FamilyMemberResponse> memberResponses = toFamilyMemberResponses(targets, subscriptionMap, subIdToPhoneMap);

        return CreateNewFamilyResponse.builder()
                .familyId(familyId)
                .applyType(applyType)
                .familyMemberList(memberResponses)
                .build();
    }

    /**
     * 구성원 추가 신청 응답 변환 (AddFamilyMemberResponse 전용)
     */
    public static AddFamilyMemberResponse toAddFamilyMemberResponse(
            Long familyId,
            ApplyType applyType,
            List<FamilyApplyTarget> targets,
            Map<Long, Subscription> subscriptionMap,
            Map<Long, String> subIdToPhoneMap) {

        List<FamilyMemberResponse> memberResponses = toFamilyMemberResponses(targets, subscriptionMap, subIdToPhoneMap);

        return AddFamilyMemberResponse.builder()
                .familyId(familyId)
                .applyType(applyType)
                .familyMemberList(memberResponses)
                .build();
    }

    /**
     * 구성원 리스트 변환 공통 로직
     */
    private static List<FamilyMemberResponse> toFamilyMemberResponses(
            List<FamilyApplyTarget> targets,
            Map<Long, Subscription> subscriptionMap,
            Map<Long, String> subIdToPhoneMap) {

        return targets.stream()
                .map(target -> {
                    Subscription sub = subscriptionMap.get(target.getTargetSubId());
                    return FamilyMemberResponse.builder()
                            .name(sub.getMember().getName())
                            .phone(subIdToPhoneMap.get(sub.getId()))
                            .targetFamilyRole(target.getTargetFamilyRole())
                            .build();
                })
                .toList();
    }
}
