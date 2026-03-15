package hotspot.user.familyReport.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.DayOfWeek;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.Family;
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.infrastructure.entity.FamilyReportEntity;

@ExtendWith(MockitoExtension.class)
class FamilyReportRepositoryImplTest {

    @Mock
    private FamilyReportJpaRepository familyReportJpaRepository;

    @InjectMocks
    private FamilyReportRepositoryImpl familyReportRepository;

    @Test
    @DisplayName("가족 ID로 리포트 구독 정보를 조회할 수 있다")
    void findByFamilyIdSuccess() {
        Long familyId = 1L;
        FamilyReportEntity entity = FamilyReportEntity.builder()
                .familyReportId(10L)
                .family(FamilyEntity.builder().familyId(familyId).build())
                .receiveDay(DayOfWeek.MONDAY)
                .isActive(true)
                .build();

        given(familyReportJpaRepository.findByFamilyFamilyId(familyId))
                .willReturn(Optional.of(entity));

        Optional<FamilyReport> result = familyReportRepository.findByFamilyId(familyId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getReceiveDay()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("가족 리포트 구독 정보를 저장할 수 있다")
    void saveSuccess() {
        FamilyReport familyReport = FamilyReport.builder()
                .family(Family.builder().id(1L).build())
                .receiveDay(DayOfWeek.FRIDAY)
                .isActive(true)
                .build();

        FamilyReportEntity savedEntity = FamilyReportEntity.builder()
                .familyReportId(11L)
                .family(FamilyEntity.builder().familyId(1L).build())
                .receiveDay(DayOfWeek.FRIDAY)
                .isActive(true)
                .build();

        given(familyReportJpaRepository.save(any(FamilyReportEntity.class))).willReturn(savedEntity);

        FamilyReport result = familyReportRepository.save(familyReport);

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getFamily().getId()).isEqualTo(1L);
        assertThat(result.getReceiveDay()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(result.isActive()).isTrue();
        verify(familyReportJpaRepository).save(any(FamilyReportEntity.class));
    }

    @Test
    @DisplayName("가족별 수신 요일을 수정할 수 있다")
    void updateReceiveDaySuccess() {
        familyReportRepository.updateReceiveDay(1L, DayOfWeek.SUNDAY);

        verify(familyReportJpaRepository).updateReceiveDay(1L, DayOfWeek.SUNDAY);
    }

    @Test
    @DisplayName("가족별 활성 상태를 수정할 수 있다")
    void updateActiveSuccess() {
        familyReportRepository.updateActive(1L, true, false);

        verify(familyReportJpaRepository).updateActive(1L, true, false);
    }
}
