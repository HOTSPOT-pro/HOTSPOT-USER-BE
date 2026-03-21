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
import hotspot.user.family.domain.Family;
import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;

@ExtendWith(MockitoExtension.class)
class CreateFamilyReportSubscriptionServiceImplTest {

    @Mock
    private FamilyReportRepository familyReportRepository;

    @InjectMocks
    private CreateFamilyReportSubscriptionServiceImpl service;

    @Test
    @DisplayName("가족 OWNER는 신규 리포트 구독을 신청할 수 있다")
    void createSubscriptionSuccess() {
        CreateFamilyReportSubscriptionRequest request =
                new CreateFamilyReportSubscriptionRequest(DayOfWeek.WEDNESDAY);

        given(familyReportRepository.findByFamilyId(1L)).willReturn(Optional.empty());
        given(familyReportRepository.save(any(FamilyReport.class)))
                .willReturn(FamilyReport.builder()
                        .id(10L)
                        .family(Family.builder().id(1L).build())
                        .receiveDay(DayOfWeek.WEDNESDAY)
                        .isActive(true)
                        .build());

        service.createSubscription(1L, FamilyRole.OWNER, request);

        verify(familyReportRepository).save(any(FamilyReport.class));
    }

    @Test
    @DisplayName("기존 구독 이력이 있으면 수신 요일을 변경하고 재활성화한다")
    void createSubscriptionReactivateSuccess() {
        CreateFamilyReportSubscriptionRequest request =
                new CreateFamilyReportSubscriptionRequest(DayOfWeek.FRIDAY);

        given(familyReportRepository.findByFamilyId(1L))
                .willReturn(Optional.of(FamilyReport.builder()
                        .id(10L)
                        .family(Family.builder().id(1L).build())
                        .receiveDay(DayOfWeek.MONDAY)
                        .isActive(false)
                        .build()));
        given(familyReportRepository.save(any(FamilyReport.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.createSubscription(1L, FamilyRole.OWNER, request);

        verify(familyReportRepository).save(any(FamilyReport.class));
    }

    @Test
    @DisplayName("가족 OWNER가 아니면 리포트 구독 신청이 불가능하다")
    void createSubscriptionDenied() {
        CreateFamilyReportSubscriptionRequest request =
                new CreateFamilyReportSubscriptionRequest(DayOfWeek.WEDNESDAY);

        assertThatThrownBy(() -> service.createSubscription(1L, FamilyRole.CHILD, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }
}
