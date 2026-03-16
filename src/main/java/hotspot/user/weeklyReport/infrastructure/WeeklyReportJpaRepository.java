package hotspot.user.weeklyReport.infrastructure;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.weeklyReport.infrastructure.entity.WeeklyReportEntity;

public interface WeeklyReportJpaRepository extends JpaRepository<WeeklyReportEntity, Long> {

    @Query("""
            SELECT wr.weeklyReportId as weeklyReportId, wr.subId as subId
            FROM WeeklyReportEntity wr
            WHERE wr.subId IN :subIds
              AND wr.weekEndDate BETWEEN :currentWeekStartDateMinusOneDay AND :currentWeekEndDateMinusOneDay
              AND wr.reportStatus = hotspot.user.weeklyReport.domain.ReportStatus.COMPLETED
            """)
    List<WeeklyReportIdRow> findCompletedCurrentWeekReportIdsBySubIds(
            @Param("subIds") List<Long> subIds,
            @Param("currentWeekStartDateMinusOneDay") LocalDate currentWeekStartDateMinusOneDay,
            @Param("currentWeekEndDateMinusOneDay") LocalDate currentWeekEndDateMinusOneDay
    );

    interface WeeklyReportIdRow {
        Long getWeeklyReportId();
        Long getSubId();
    }
}
