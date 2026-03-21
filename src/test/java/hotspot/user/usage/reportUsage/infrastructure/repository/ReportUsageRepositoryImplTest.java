package hotspot.user.usage.reportUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportUsageRepositoryImplTest {

    @Mock
    private ReportUsageRedisRepository redisRepository;

    @InjectMocks
    private ReportUsageRepositoryImpl repository;

    @Test
    @DisplayName("일별 사용량 조회 위임 테스트")
    void shouldDelegateDailyCall() {

        List<Long> subIds = List.of(1L);
        List<LocalDate> dates = List.of(LocalDate.of(2026, 2, 1));

        Map<Long, Map<LocalDate, Double>> expected =
                Map.of(1L, Map.of(LocalDate.of(2026, 2, 1), 2.0));

        when(redisRepository.findReportUsageDailyGb(subIds, dates))
                .thenReturn(expected);

        Map<Long, Map<LocalDate, Double>> result =
                repository.findReportUsageDailyGb(subIds, dates);

        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findReportUsageDailyGb(subIds, dates);
    }

    @Test
    @DisplayName("월별 사용량 조회 위임 테스트")
    void shouldDelegateMonthlyCall() {

        List<Long> subIds = List.of(1L);
        List<YearMonth> months = List.of(YearMonth.of(2026, 2));

        Map<Long, Map<YearMonth, Double>> expected =
                Map.of(1L, Map.of(YearMonth.of(2026, 2), 5.0));

        when(redisRepository.findReportUsageMonthlyGb(subIds, months))
                .thenReturn(expected);

        Map<Long, Map<YearMonth, Double>> result =
                repository.findReportUsageMonthlyGb(subIds, months);

        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findReportUsageMonthlyGb(subIds, months);
    }
}
