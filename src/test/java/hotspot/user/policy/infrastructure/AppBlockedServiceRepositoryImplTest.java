package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Set;

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
        // ... (기존 코드 생략)
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

        List<AppBlockedService> result = appBlockedServiceRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("YouTube");
        assertThat(result.get(1).getName()).isEqualTo("TikTok");
    }

    @Test
    @DisplayName("전달받은 ID 리스트에 해당하는 서비스 개수를 조회한다")
    void countByIdInSuccess() {
        // given
        Set<Long> ids = Set.of(1L, 2L);
        given(appBlockedServiceJpaRepository.countByAppBlockedServiceIdIn(ids)).willReturn(2L);

        // when
        long count = appBlockedServiceRepository.countByIdIn(ids);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("삭제되지 않은 ID 목록으로 조회 후 도메인 변환한다")
    void findAllByAppBlackedServiceIdsSuccess() {

        // given
        List<Long> ids = List.of(1L, 2L);

        AppBlockedServiceEntity entity1 = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(1L)
                .blockedServiceName("YouTube")
                .blockedServiceCode("YOUTUBE")
                .isActive(true)
                .build();

        AppBlockedServiceEntity entity2 = AppBlockedServiceEntity.builder()
                .appBlockedServiceId(2L)
                .blockedServiceName("TikTok")
                .blockedServiceCode("TIKTOK")
                .isActive(true)
                .build();

        given(appBlockedServiceJpaRepository.findByIdInAndIsActiveTrue(ids))
                .willReturn(List.of(entity1, entity2));

        // when
        List<AppBlockedService> result =
                appBlockedServiceRepository.findAllByAppBlockedServiceIds(ids);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("YouTube");
        assertThat(result.get(1).getName()).isEqualTo("TikTok");
    }
}
