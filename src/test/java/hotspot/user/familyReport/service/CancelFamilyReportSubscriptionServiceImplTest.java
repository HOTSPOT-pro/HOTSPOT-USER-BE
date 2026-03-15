package hotspot.user.familyReport.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyReportErrorCode;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;

@ExtendWith(MockitoExtension.class)
class CancelFamilyReportSubscriptionServiceImplTest {

    @Mock
    private FamilyReportRepository familyReportRepository;

    @InjectMocks
    private CancelFamilyReportSubscriptionServiceImpl service;

    @Test
    @DisplayName("가족 OWNER는 활성 구독을 취소할 수 있다")
    void cancelSubscriptionSuccess() {
        given(familyReportRepository.updateActive(1L, true, false)).willReturn(1);

        service.cancelSubscription(1L, FamilyRole.OWNER);
    }

    @Test
    @DisplayName("가족 OWNER가 아니면 구독을 취소할 수 없다")
    void cancelSubscriptionDenied() {
        assertThatThrownBy(() -> service.cancelSubscription(1L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("활성 구독이 없으면 취소할 수 없다")
    void cancelSubscriptionNotFound() {
        given(familyReportRepository.updateActive(1L, true, false)).willReturn(0);

        assertThatThrownBy(() -> service.cancelSubscription(1L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyReportErrorCode.FAMILY_REPORT_SUBSCRIPTION_NOT_FOUND);
    }
}
