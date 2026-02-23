package hotspot.user.family.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;


/**
 * 가족 신청 관리 테이블에 실제로 저장하는 JpaRepository
 */
public interface FamilyApplyJpaRepository extends JpaRepository<FamilyApplyEntity, Long> {
}
