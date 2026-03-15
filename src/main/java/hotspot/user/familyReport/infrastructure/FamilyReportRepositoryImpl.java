package hotspot.user.familyReport.infrastructure;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.infrastructure.entity.FamilyReportEntity;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyReportRepositoryImpl implements FamilyReportRepository {

    private final FamilyReportJpaRepository familyReportJpaRepository;

    @Override
    public Optional<FamilyReport> findByFamilyId(Long familyId) {
        return familyReportJpaRepository.findByFamilyFamilyId(familyId)
                .map(FamilyReportEntity::entityToDomain);
    }

    @Override
    public FamilyReport save(FamilyReport familyReport) {
        FamilyReportEntity entity = FamilyReportEntity.domainToEntity(familyReport);
        return familyReportJpaRepository.save(entity).entityToDomain();
    }

    @Override
    public void updateReceiveDay(Long familyId, DayOfWeek receiveDay) {
        familyReportJpaRepository.updateReceiveDay(familyId, receiveDay);
    }

    @Override
    public void updateActive(Long familyId, boolean isActive) {
        familyReportJpaRepository.updateActive(familyId, isActive);
    }
}
