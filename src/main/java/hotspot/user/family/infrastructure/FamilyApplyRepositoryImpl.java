package hotspot.user.family.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.user.family.service.port.FamilyApplyRepository;
import lombok.RequiredArgsConstructor;

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

    @Override
    public boolean existsPendingApply(Long requesterSubId, Long targetSubId, Long familyId) {
        return familyApplyJpaRepository
                .existsByRequesterSubscriptionSubIdAndTargetSubscriptionSubIdAndFamilyFamilyIdAndStatus(
                requesterSubId, targetSubId, familyId, ApplyStatus.PENDING);
    }
}
