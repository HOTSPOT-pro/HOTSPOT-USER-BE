package hotspot.user.familyReport.service.port;

import java.time.DayOfWeek;
import java.util.Optional;

import hotspot.user.familyReport.domain.FamilyReport;

public interface FamilyReportRepository {

    Optional<FamilyReport> findByFamilyId(Long familyId);

    FamilyReport save(FamilyReport familyReport);

    int updateReceiveDay(Long familyId, DayOfWeek receiveDay);

    int updateActive(Long familyId, boolean currentIsActive, boolean newIsActive);
}
