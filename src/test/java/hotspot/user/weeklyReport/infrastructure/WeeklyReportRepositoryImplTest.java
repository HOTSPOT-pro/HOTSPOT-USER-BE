package hotspot.user.weeklyReport.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.infrastructure.entity.WeeklyReportEntity;

@ExtendWith(MockitoExtension.class)
class WeeklyReportRepositoryImplTest {

    @Mock
    private WeeklyReportJpaRepository weeklyReportJpaRepository;

    @InjectMocks
    private WeeklyReportRepositoryImpl repository;

    @Test
    @DisplayName("리포트 ID로 조회 시 엔티티를 도메인으로 변환하여 반환한다")
    void findByReportIdSuccess() {
        // given
        Long reportId = 1L;
        WeeklyReportEntity entity = WeeklyReportEntity.builder()
                .weeklyReportId(reportId)
                .subId(10L)
                .name("Test Report")
                .build();

        given(weeklyReportJpaRepository.findById(reportId)).willReturn(Optional.of(entity));

        // when
        Optional<WeeklyReport> result = repository.findByReportId(reportId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getWeeklyReportId()).isEqualTo(reportId);
        assertThat(result.get().getName()).isEqualTo("Test Report");
    }

    @Test
    @DisplayName("리포트가 없는 경우 Optional.empty를 반환한다")
    void findByReportIdEmpty() {
        // given
        Long reportId = 999L;
        given(weeklyReportJpaRepository.findById(reportId)).willReturn(Optional.empty());

        // when
        Optional<WeeklyReport> result = repository.findByReportId(reportId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회선과 월로 월별 주간 리포트 목록을 조회할 수 있다")
    void findMonthlyReportsBySubIdSuccess() {
        WeeklyReportEntity latestEntity = WeeklyReportEntity.builder()
                .weeklyReportId(101L)
                .subId(10L)
                .weekStartDate(LocalDate.of(2026, 3, 11))
                .weekEndDate(LocalDate.of(2026, 3, 17))
                .build();
        WeeklyReportEntity olderEntity = WeeklyReportEntity.builder()
                .weeklyReportId(100L)
                .subId(10L)
                .weekStartDate(LocalDate.of(2026, 3, 4))
                .weekEndDate(LocalDate.of(2026, 3, 10))
                .build();

        given(weeklyReportJpaRepository.findMonthlyReportsBySubId(
                10L,
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 30)))
                .willReturn(List.of(latestEntity, olderEntity));

        List<WeeklyReport> result = repository.findMonthlyReportsBySubId(10L, java.time.YearMonth.of(2026, 3));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWeeklyReportId()).isEqualTo(101L);
        assertThat(result.get(1).getWeeklyReportId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("회선 목록과 현재 주차 범위로 이번 주 완료 리포트 ID를 조회할 수 있다")
    void findCompletedCurrentWeekReportIdsBySubIdsSuccess() {
        WeeklyReportJpaRepository.WeeklyReportIdRow row1 = mock(WeeklyReportJpaRepository.WeeklyReportIdRow.class);
        WeeklyReportJpaRepository.WeeklyReportIdRow row2 = mock(WeeklyReportJpaRepository.WeeklyReportIdRow.class);

        given(row1.getSubId()).willReturn(10L);
        given(row1.getWeeklyReportId()).willReturn(101L);
        given(row2.getSubId()).willReturn(11L);
        given(row2.getWeeklyReportId()).willReturn(102L);

        given(weeklyReportJpaRepository.findCompletedCurrentWeekReportIdsBySubIds(
                List.of(10L, 11L),
                LocalDate.of(2026, 3, 15),
                LocalDate.of(2026, 3, 21)))
                .willReturn(List.of(row1, row2));

        Map<Long, Long> result = repository.findCompletedCurrentWeekReportIdsBySubIds(
                List.of(10L, 11L),
                LocalDate.of(2026, 3, 16),
                LocalDate.of(2026, 3, 22)
        );

        assertThat(result).containsEntry(10L, 101L);
        assertThat(result).containsEntry(11L, 102L);
    }
}
