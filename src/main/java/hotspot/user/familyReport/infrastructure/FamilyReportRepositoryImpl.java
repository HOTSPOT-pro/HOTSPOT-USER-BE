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
    public Optional<DayOfWeek> findActiveReceiveDayByFamilyId(Long familyId) {
        return familyReportJpaRepository.findActiveReceiveDayByFamilyId(familyId);
    }

    @Override
    public FamilyReport save(FamilyReport familyReport) {
        FamilyReportEntity entity = FamilyReportEntity.domainToEntity(familyReport);
        return familyReportJpaRepository.save(entity).entityToDomain();
    }

    @Override
    public int updateReceiveDay(Long familyId, DayOfWeek receiveDay) {
        return familyReportJpaRepository.updateReceiveDay(familyId, receiveDay);
    }

    @Override
    public int updateActive(Long familyId, boolean currentIsActive, boolean newIsActive) {
        return familyReportJpaRepository.updateActive(familyId, currentIsActive, newIsActive);
    }
}
