package hotspot.user.familyReport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.Family;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.service.port.FamilyReportRepository;

@ExtendWith(MockitoExtension.class)
class FindFamilyReportSubscriptionServiceImplTest {

    @Mock
    private FamilyReportRepository familyReportRepository;

    @InjectMocks
    private FindFamilyReportSubscriptionServiceImpl service;

    @Test
    @DisplayName("활성화된 구독이 있으면 구독 상태와 수신 요일을 반환한다")
    void findSubscriptionSuccess() {
        given(familyReportRepository.findByFamilyId(1L))
                        .willReturn(Optional.of(FamilyReport.builder()
                        .id(10L)
                        .family(Family.builder().id(1L).build())
                        .active(true)
                        .build()));

        FamilyReportSubscriptionResponse result = service.findSubscription(1L);

        assertThat(result.subscribed()).isTrue();
    }

    @Test
    @DisplayName("구독 정보가 없으면 미구독 상태를 반환한다")
    void findSubscriptionWhenNotFound() {
        given(familyReportRepository.findByFamilyId(1L)).willReturn(Optional.empty());

        FamilyReportSubscriptionResponse result = service.findSubscription(1L);

        assertThat(result.subscribed()).isFalse();
    }

    @Test
    @DisplayName("비활성화된 구독은 미구독 상태로 반환한다")
    void findSubscriptionWhenInactive() {
        given(familyReportRepository.findByFamilyId(1L))
                        .willReturn(Optional.of(FamilyReport.builder()
                        .id(10L)
                        .family(Family.builder().id(1L).build())
                        .active(false)
                        .build()));

        FamilyReportSubscriptionResponse result = service.findSubscription(1L);

        assertThat(result.subscribed()).isFalse();
    }
}
