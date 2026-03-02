package hotspot.user.family.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyRemoveSchedule;
import hotspot.user.family.infrastructure.entity.FamilyRemoveScheduleEntity;
import hotspot.user.family.service.port.FamilyRemoveScheduleRepository;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성원 삭제 신청 스케쥴러 repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class FamilyRemoveScheduleRepositoryImpl implements FamilyRemoveScheduleRepository {
    private final FamilyRemoveScheduleJpaRepository familyRemoveScheduleJpaRepository;

    @Override
    public List<FamilyRemoveSchedule> saveAll(List<FamilyRemoveSchedule> familyRemoveScheduleList) {
        List<FamilyRemoveScheduleEntity> familyRemoveScheduleEntityList = familyRemoveScheduleList.stream()
                .map(FamilyRemoveScheduleEntity::domainToEntity)
                .toList();

        List<FamilyRemoveScheduleEntity> saved = familyRemoveScheduleJpaRepository.saveAll(familyRemoveScheduleEntityList);

        return saved.stream()
                .map(FamilyRemoveScheduleEntity::entityToDomain)
                .toList();
    }

    @Override
    public List<FamilyRemoveSchedule> findAllByTargetSubIdInAndStatus(List<Long> subIds, DeleteStatus status) {
        return familyRemoveScheduleJpaRepository.findAllByTargetSubIdInAndStatus(subIds, status).stream()
                .map(FamilyRemoveScheduleEntity::entityToDomain)
                .toList();
    }
}
