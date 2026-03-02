package hotspot.user.family.domain.mapper;

import java.time.LocalDate;
import java.util.List;

import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyRemoveSchedule;

/**
 * 가족 구성원 삭제 신청 Mapper
 */
public class RemoveFamilyMemberMapper {

    // request -> domain
    public static FamilyApply toFamilyApply(Long requesterSubId, Long familyId) {
        return FamilyApply.builder()
                .requesterSubId(requesterSubId)
                .familyId(familyId)
                .applyType(ApplyType.REMOVE)
                .status(ApplyStatus.PENDING)
                .build();
    }

    // 개별 삭제 스케줄 생성
    public static FamilyRemoveSchedule toFamilyRemoveSchedule(Long familyId, Long targetSubId, LocalDate scheduleDate) {
        return FamilyRemoveSchedule.builder()
                .familyId(familyId)
                .targetSubId(targetSubId)
                .scheduleDate(scheduleDate)
                .status(DeleteStatus.SCHEDULED)
                .build();
    }

    // domain -> response
    public static RemoveFamilyMemberResponse toRemoveFamilyMemberResponse(
            FamilyApply familyApply,
            List<FamilyRemoveSchedule> schedules) {

        List<Long> subIdList = schedules.stream()
                .map(FamilyRemoveSchedule::getTargetSubId)
                .toList();

        // 모든 스케줄의 날짜와 상태는 동일하므로 첫 번째 것을 참조
        LocalDate scheduleDate = schedules.isEmpty() ? null : schedules.get(0).getScheduleDate();
        DeleteStatus status = schedules.isEmpty() ? null : schedules.get(0).getStatus();

        return RemoveFamilyMemberResponse.builder()
                .familyApplyId(familyApply.getId())
                .familyId(familyApply.getFamilyId())
                .subIdList(subIdList)
                .status(status)
                .scheduleDate(scheduleDate)
                .build();
    }
}
