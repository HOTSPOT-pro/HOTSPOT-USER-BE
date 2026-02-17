package hotspot.user.family.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.family.infrastructure.entity.FamilyEntity;

/**
 * 가족 DB에 실제로 저장하는 JpaRepository
 */
public interface FamilyJpaRepository extends JpaRepository<FamilyEntity, Long> {
}
