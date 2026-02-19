package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;

/**
 * 차단 앱 서비스 Repository 조회 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AppBlockedServiceRepositoryImplTest {

    @Mock
    private AppBlockedServiceJpaRepository appBlockedServiceJpaRepository;

    @InjectMocks
    private AppBlockedServiceRepositoryImpl appBlockedServiceRepository;

    @Test
    @DisplayName("전체 앱 차단 서비스 목록 조회 성공: 엔티티 리스트를 도메인 리스트로 변환한다")
    void findAllSuccess() {
        // given
        AppBlockedServiceEntity entity1 = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(1L)
                .blockedServiceName("YouTube")
                .blockedServiceCode("YOUTUBE")
                .build();

        AppBlockedServiceEntity entity2 = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(2L)
                .blockedServiceName("TikTok")
                .blockedServiceCode("TIKTOK")
                .build();

        given(appBlockedServiceJpaRepository.findAll()).willReturn(List.of(entity1, entity2));

        // when
        List<AppBlockedService> result = appBlockedServiceRepository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("YouTube");
        assertThat(result.get(1).getName()).isEqualTo("TikTok");
    }
}
