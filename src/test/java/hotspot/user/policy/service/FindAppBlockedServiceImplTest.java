package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;

/**
 * 관리자 앱 차단 서비스 단위 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class FindAppBlockedServiceImplTest {

    @Mock
    private AppBlockedServiceRepository appBlockedServiceRepository;

    @InjectMocks
    private FindAppBlockedServiceImpl findAppBlockedService;

    @Test
    @DisplayName("앱 차단 서비스 목록 조회 성공: 도메인을 Response DTO로 변환하여 반환한다")
    void findAllSuccess() {
        // given
        AppBlockedService domain1 = AppBlockedService.builder()
                .id(1L)
                .name("YouTube")
                .serviceCode("YOUTUBE")
                .build();

        given(appBlockedServiceRepository.findAll()).willReturn(List.of(domain1));

        // when
        List<AppBlockedServiceResponse> result = findAppBlockedService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("YouTube");
        assertThat(result.get(0).serviceCode()).isEqualTo("YOUTUBE");
    }
}
