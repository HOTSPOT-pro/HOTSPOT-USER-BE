package hotspot.user.familyReport.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyReportErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.familyReport.controller.request.UpdateFamilyReportReceiveDayRequest;
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyReportReceiveDayServiceImplTest {

    @Mock
    private FamilyReportRepository familyReportRepository;

    @InjectMocks
    private UpdateFamilyReportReceiveDayServiceImpl service;

    @Test
    @DisplayName("가족 OWNER는 활성 구독의 수신 요일을 변경할 수 있다")
    void updateReceiveDaySuccess() {
        UpdateFamilyReportReceiveDayRequest request =
                new UpdateFamilyReportReceiveDayRequest(DayOfWeek.SUNDAY);

        given(familyReportRepository.findByFamilyId(1L))
                .willReturn(Optional.of(FamilyReport.builder()
                        .id(10L)
                        .family(Family.builder().id(1L).build())
                        .receiveDay(DayOfWeek.MONDAY)
                        .active(true)
                        .build()));
        given(familyReportRepository.save(any(FamilyReport.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.updateReceiveDay(1L, FamilyRole.OWNER, request);

        verify(familyReportRepository).save(any(FamilyReport.class));
    }

    @Test
    @DisplayName("가족 OWNER가 아니면 수신 요일을 변경할 수 없다")
    void updateReceiveDayDenied() {
        UpdateFamilyReportReceiveDayRequest request =
                new UpdateFamilyReportReceiveDayRequest(DayOfWeek.SUNDAY);

        assertThatThrownBy(() -> service.updateReceiveDay(1L, FamilyRole.CHILD, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("활성 구독이 없으면 수신 요일을 변경할 수 없다")
    void updateReceiveDayNotFound() {
        UpdateFamilyReportReceiveDayRequest request =
                new UpdateFamilyReportReceiveDayRequest(DayOfWeek.SUNDAY);

        given(familyReportRepository.findByFamilyId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateReceiveDay(1L, FamilyRole.OWNER, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyReportErrorCode.FAMILY_REPORT_SUBSCRIPTION_NOT_FOUND);
    }
}
