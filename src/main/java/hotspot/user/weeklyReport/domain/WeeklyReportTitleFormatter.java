package hotspot.user.weeklyReport.domain;

import java.time.LocalDate;

public final class WeeklyReportTitleFormatter {

    private WeeklyReportTitleFormatter() {
    }

    public static String format(WeeklyReport weeklyReport) {
        return format(weeklyReport.getWeekEndDate());
    }

    public static String format(LocalDate weekEndDate) {
        LocalDate issuedDate = weekEndDate.plusDays(1);
        int weekOrder = ((issuedDate.getDayOfMonth() - 1) / 7) + 1;

        return "%d년 %d월 %d주차 분석 리포트".formatted(
                issuedDate.getYear(),
                issuedDate.getMonthValue(),
                weekOrder
        );
    }
}
