package hotspot.user.usage.reportUsage.service.port;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface ReportUsageRepository {

    Map<Long, Map<LocalDate, Double>> findReportUsageDailyGb(
            List<Long> subIds,
            List<LocalDate> dates
    );

    Map<Long, Map<YearMonth, Double>> findReportUsageMonthlyGb(
            List<Long> subIds,
            List<YearMonth> months
    );
}
