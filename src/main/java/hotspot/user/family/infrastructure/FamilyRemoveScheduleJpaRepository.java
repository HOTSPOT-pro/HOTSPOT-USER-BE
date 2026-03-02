package hotspot.user.family.infrastructure;

import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.user.family.infrastructure.entity.FamilyRemoveScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 가족 구성원 삭제 스케쥴러
 */
public interface FamilyRemoveScheduleJpaRepository extends JpaRepository<FamilyRemoveScheduleEntity, Long> {
}
