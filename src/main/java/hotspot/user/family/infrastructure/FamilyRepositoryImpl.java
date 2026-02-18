package hotspot.user.family.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.family.domain.Family;
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryImpl implements FamilyRepository {

    private final FamilyJpaRepository familyJpaRepository;
    @Override
    public Optional<Family> findById(Long id) {
        return familyJpaRepository.findById(id)
                .map(FamilyEntity::entityToDomain);
    }
}
