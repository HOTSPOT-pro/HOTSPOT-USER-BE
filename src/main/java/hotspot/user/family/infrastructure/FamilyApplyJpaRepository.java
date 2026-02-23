package hotspot.user.family.infrastructure;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 가족 신청 관리 테이블에 실제로 저장하는 JpaRepository
 */
public interface FamilyApplyJpaRepository extends JpaRepository<FamilyApplyEntity, Long> {

    // 동일한 신청자-피신청자-PENDING인 신청이 있는지 확인
    boolean existsByRequesterSubscriptionSubIdAndTargetSubscriptionSubIdAndFamilyFamilyIdAndStatus(
            Long requesterSubId, Long targetSubId, Long familyId, ApplyStatus status);
}
