package hotspot.user.family.service.port;

import java.util.Optional;

import hotspot.user.family.domain.Family;

/**
 * 가족 도메인에 저장하는 Repository
 */
public interface FamilyRepository {
    Optional<Family> findById(Long id);
    Family save(Family family);
}
