package hotspot.user.family.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.infrastructure.entity.FamilyApplyTargetEntity;
import hotspot.user.family.service.port.FamilyApplyTargetRepository;
import lombok.RequiredArgsConstructor;

/**
 * 가족 생성 / 구성원 추가 / 신청 타겟 repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class FamilyApplyTargetRepositoryImpl implements FamilyApplyTargetRepository {
    private final FamilyApplyTargetJpaRepository familyApplyTargetJpaRepository;

    @Override
    public FamilyApplyTarget save(FamilyApplyTarget familyApplyTarget) {
        FamilyApplyTargetEntity entity = FamilyApplyTargetEntity.domainToEntity(familyApplyTarget);
        FamilyApplyTargetEntity saved = familyApplyTargetJpaRepository.save(entity);
        return saved.entityToDomain();
    }

    @Override
    public List<FamilyApplyTarget> saveAll(List<FamilyApplyTarget> familyApplyTargetList) {
        List<FamilyApplyTargetEntity> entities = familyApplyTargetList.stream()
                .map(FamilyApplyTargetEntity::domainToEntity)
                .toList();

        List<FamilyApplyTargetEntity> savedEntities = familyApplyTargetJpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(FamilyApplyTargetEntity::entityToDomain)
                .toList();
    }

    @Override
    public boolean existsPendingApplyByTargetSubId(Long targetSubId) {
        return familyApplyTargetJpaRepository.existsByTargetSubIdAndFamilyApplyStatus(targetSubId, ApplyStatus.PENDING);
    }
}
