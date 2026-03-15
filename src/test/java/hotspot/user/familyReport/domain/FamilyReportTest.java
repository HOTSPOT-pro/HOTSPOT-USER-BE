package hotspot.user.familyReport.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.family.domain.Family;

class FamilyReportTest {

    @Test
    @DisplayName("수신 요일을 변경할 수 있다")
    void updateReceiveDaySuccess() {
        FamilyReport familyReport = FamilyReport.builder()
                .id(1L)
                .family(Family.builder().id(10L).build())
                .receiveDay(DayOfWeek.MONDAY)
                .isActive(true)
                .build();

        familyReport.updateReceiveDay(DayOfWeek.FRIDAY);

        assertThat(familyReport.getReceiveDay()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("비활성 구독을 활성화할 수 있다")
    void activateSuccess() {
        FamilyReport familyReport = FamilyReport.builder()
                .id(1L)
                .family(Family.builder().id(10L).build())
                .receiveDay(DayOfWeek.MONDAY)
                .isActive(false)
                .build();

        familyReport.activate();

        assertThat(familyReport.isActive()).isTrue();
    }

    @Test
    @DisplayName("활성 구독을 비활성화할 수 있다")
    void deactivateSuccess() {
        FamilyReport familyReport = FamilyReport.builder()
                .id(1L)
                .family(Family.builder().id(10L).build())
                .receiveDay(DayOfWeek.MONDAY)
                .isActive(true)
                .build();

        familyReport.deactivate();

        assertThat(familyReport.isActive()).isFalse();
    }
}
