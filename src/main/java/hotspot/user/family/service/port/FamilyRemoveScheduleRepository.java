package hotspot.user.family.service.port;

import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyRemoveSchedule;

import java.util.List;

/**
 * 가족 구성원 삭제 신청 repository
 */
public interface FamilyRemoveScheduleRepository {
    List<FamilyRemoveSchedule> saveAll(List<FamilyRemoveSchedule> familyRemoveScheduleList);
    List<FamilyRemoveSchedule> findAllByTargetSubIdInAndStatus(List<Long> subIds, DeleteStatus status);
}
