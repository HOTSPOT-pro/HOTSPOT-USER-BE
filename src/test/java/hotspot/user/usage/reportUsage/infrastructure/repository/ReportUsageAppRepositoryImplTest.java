package hotspot.user.usage.reportUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.usage.reportUsage.domain.AppUsage;

@ExtendWith(MockitoExtension.class)
class ReportUsageAppRepositoryImplTest {

    @Mock
    private ReportUsageAppRedisRepository redisRepository;

    @InjectMocks
    private ReportUsageAppRepositoryImpl repository;

    @Test
    @DisplayName("월별 앱 사용량 조회 위임 테스트")
    void shouldDelegateMonthlyCall() {

        Long subId = 1L;

        List<AppUsage> expected =
                List.of(new AppUsage(100L, 3.5));

        when(redisRepository.findMonthlyAppUsage(subId))
                .thenReturn(expected);

        List<AppUsage> result =
                repository.findMonthlyAppUsage(subId);

        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findMonthlyAppUsage(subId);
    }

    @Test
    @DisplayName("일별 앱 사용량 조회 위임 테스트")
    void shouldDelegateDailyCall() {

        Long subId = 1L;

        List<AppUsage> expected =
                List.of(new AppUsage(200L, 1.2));

        when(redisRepository.findDailyAppUsage(subId, LocalDate.now()))
                .thenReturn(expected);

        List<AppUsage> result =
                repository.findDailyAppUsage(subId, LocalDate.now());

        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findDailyAppUsage(subId, LocalDate.now());
    }
}
