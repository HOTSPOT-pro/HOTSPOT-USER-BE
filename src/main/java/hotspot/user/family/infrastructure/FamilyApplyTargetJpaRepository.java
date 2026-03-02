package hotspot.user.family.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.infrastructure.entity.FamilyApplyTargetEntity;

import java.util.List;

/**
 * 가족 생성 / 구성원 추가 / 신청 타겟 Jpa Repository
 */
public interface FamilyApplyTargetJpaRepository extends JpaRepository<FamilyApplyTargetEntity, Long> {
    boolean existsByTargetSubIdAndFamilyApplyStatus(Long targetSubId, ApplyStatus status);
    List<FamilyApplyTargetEntity> findAllByTargetSubIdInAndFamilyApplyStatus(List<Long> subIds, ApplyStatus status);
}
