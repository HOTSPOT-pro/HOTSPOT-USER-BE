package hotspot.user.family.infrastructure;

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
import hotspot.user.family.infrastructure.entity.FamilyEntity;

/**
 * 가족 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FamilyRepositoryImplTest {

    @Mock
    private FamilyJpaRepository familyJpaRepository;

    @InjectMocks
    private FamilyRepositoryImpl familyRepository;

    @Test
    @DisplayName("가족 ID로 가족 정보 조회 성공")
    void findByIdSuccess() {
        // given
        Long familyId = 1L;
        FamilyEntity entity = FamilyEntity.builder()
                .familyId(familyId)
                .build();

        given(familyJpaRepository.findById(familyId)).willReturn(Optional.of(entity));

        // when
        Optional<Family> result = familyRepository.findById(familyId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(familyId);
    }
}
