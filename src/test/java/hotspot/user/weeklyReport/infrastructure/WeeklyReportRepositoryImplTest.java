package hotspot.user.weeklyReport.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
}
