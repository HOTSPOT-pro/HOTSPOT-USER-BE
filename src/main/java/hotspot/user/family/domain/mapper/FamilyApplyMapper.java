package hotspot.user.family.domain.mapper;

import java.util.List;
import java.util.Map;

import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
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

    // 단건 신청용 (기존 CreateFamilyApplyRequest 대응)
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId, CreateFamilyApplyRequest request) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(request.applyType())
                .docUrl(request.docUrl())
                .status(ApplyStatus.PENDING)
                .build();
    }

    // 다건 신청용 (AddFamilyMemberRequest 대응)
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId, AddFamilyMemberRequest request) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(request.applyType())
                .docUrl(request.docUrl())
                .status(ApplyStatus.PENDING)
                .build();
    }

    // 타겟 도메인 생성
    public static FamilyApplyTarget toFamilyApplyTarget(Long familyApplyId, Long targetSubId, FamilyRole role) {
        return FamilyApplyTarget.builder()
                .familyApplyId(familyApplyId)
                .targetSubId(targetSubId)
                .targetFamilyRole(role)
                .build();
    }

    // 단건 응답 변환
    public static CreateFamilyApplyResponse toCreateFamilyApplyResponse(FamilyApply familyApply, Long targetSubId, FamilyRole role) {
        return CreateFamilyApplyResponse.builder()
                .targetSubId(targetSubId)
                .familyId(familyApply.getFamilyId())
                .applyType(familyApply.getApplyType())
                .targetFamilyRole(role)
                .docUrl(familyApply.getDocUrl())
                .status(familyApply.getStatus())
                .build();
    }

    /**
     * 다건 응답 변환 (Domain -> Response)
     * - 매퍼의 순수성을 위해 복호화 도구가 아닌 복호화된 데이터를 전달받음
     */
    public static AddFamilyMemberResponse toAddFamilyMemberResponse(
            Long familyId,
            ApplyType applyType,
            List<FamilyApplyTarget> targets,
            Map<Long, Subscription> subscriptionMap,
            Map<Long, String> subIdToPhoneMap) {

        List<FamilyMemberResponse> memberResponses = targets.stream()
                .map(target -> {
                    Subscription sub = subscriptionMap.get(target.getTargetSubId());
                    return FamilyMemberResponse.builder()
                            .name(sub.getMember().getName())
                            .phone(subIdToPhoneMap.get(sub.getId()))
                            .targetFamilyRole(target.getTargetFamilyRole())
                            .build();
                })
                .toList();

        return AddFamilyMemberResponse.builder()
                .familyId(familyId)
                .applyType(applyType)
                .familyMemberList(memberResponses)
                .build();
    }
}
