package hotspot.user.family.service.port;

import java.util.List;

import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyRemoveSchedule;

/**
 * 가족 구성원 삭제 신청 repository
 */
public interface FamilyRemoveScheduleRepository {
    List<FamilyRemoveSchedule> saveAll(List<FamilyRemoveSchedule> familyRemoveScheduleList);
    List<FamilyRemoveSchedule> findAllByTargetSubIdInAndStatus(List<Long> subIds, DeleteStatus status);
}
