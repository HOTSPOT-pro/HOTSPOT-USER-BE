package hotspot.user.familyReport.controller.request;

import java.time.DayOfWeek;

import jakarta.validation.constraints.NotNull;

public record UpdateFamilyReportReceiveDayRequest(
        @NotNull(message = "리포트 수신 요일은 필수입니다.")
        DayOfWeek receiveDay
) {
}
