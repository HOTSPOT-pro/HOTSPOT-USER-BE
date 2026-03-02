package hotspot.user.family.infrastructure;

import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.infrastructure.entity.FamilyRemoveScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 가족 구성원 삭제 스케쥴러
 */
public interface FamilyRemoveScheduleJpaRepository extends JpaRepository<FamilyRemoveScheduleEntity, Long> {
    List<FamilyRemoveScheduleEntity> findAllByTargetSubIdInAndStatus(List<Long> targetSubIds, DeleteStatus status);
}
