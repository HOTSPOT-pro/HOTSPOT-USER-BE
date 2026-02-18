package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.family.controller.response.FamilyResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.service.port.FamilyRepository;

/**
 * 가족 조회 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FindFamilyServiceImplTest {

    @Mock
    private FamilyRepository familyRepository;

    @InjectMocks
    private FindFamilyServiceImpl findFamilyService;

    @Test
    @DisplayName("가족 정보 조회 성공: 가족 ID로 가족 정보를 조회한다")
    void findFamilySuccess() {
        // given
        Long familyId = 100L;
        Family family = Family.builder()
                .id(familyId)
                .build();

        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));

        // when
        FamilyResponse result = findFamilyService.findById(familyId);

        // then
        assertThat(result.id()).isEqualTo(familyId);
    }

    @Test
    @DisplayName("가족 정보 조회 실패: 존재하지 않는 가족 ID일 때 예외가 발생한다")
    void findFamilyFailNotFound() {
        // given
        Long invalidId = 999L;
        given(familyRepository.findById(invalidId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findFamilyService.findById(invalidId))
                .isInstanceOf(ApplicationException.class);
    }
}
