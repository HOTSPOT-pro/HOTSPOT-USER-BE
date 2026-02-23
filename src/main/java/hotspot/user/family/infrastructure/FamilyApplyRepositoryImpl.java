package hotspot.user.family.infrastructure;

import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.user.family.service.port.FamilyApplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FamilyApplyRepositoryImpl implements FamilyApplyRepository {
    private final FamilyApplyJpaRepository familyApplyJpaRepository;

    @Override
    public FamilyApply save(FamilyApply familyApply) {
        FamilyApplyEntity entity = FamilyApplyEntity.domainToEntity(familyApply);
        FamilyApplyEntity saved = familyApplyJpaRepository.save(entity);
        return saved.entityToDomain();
    }
}
