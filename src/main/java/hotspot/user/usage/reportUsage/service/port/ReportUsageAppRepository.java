package hotspot.user.usage.reportUsage.service.port;

import java.time.LocalDate;
import java.util.List;

import hotspot.user.usage.reportUsage.domain.AppUsage;

public interface ReportUsageAppRepository {

    List<AppUsage> findMonthlyAppUsage(Long subId);

    List<AppUsage> findDailyAppUsage(Long subId, LocalDate date);
}
