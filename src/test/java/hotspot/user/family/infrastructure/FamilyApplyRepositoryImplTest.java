package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;

@ExtendWith(MockitoExtension.class)
class FamilyApplyRepositoryImplTest {

    @InjectMocks
    private FamilyApplyRepositoryImpl familyApplyRepository;

    @Mock
    private FamilyApplyJpaRepository familyApplyJpaRepository;

    @Mock
    private FamilyApplyTargetJpaRepository familyApplyTargetJpaRepository;

    @Test
    @DisplayName("성공: 도메인 객체를 저장하고 다시 도메인으로 반환한다")
    void saveSuccess() {
        // given
        FamilyApply domain = FamilyApply.builder()
                .requesterSubId(1L).familyId(100L)
                .applyType(ApplyType.ADD).build();

        FamilyApplyEntity entity = FamilyApplyEntity.domainToEntity(domain);
        given(familyApplyJpaRepository.save(any(FamilyApplyEntity.class))).willReturn(entity);

        // when
        FamilyApply result = familyApplyRepository.save(domain);

        // then
        assertThat(result.getRequesterSubId()).isEqualTo(1L);
        assertThat(result.getFamilyId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("성공: 이미 대기 중인 신청이 있는지 확인한다 (존재함)")
    void existsPendingApplyTrue() {
        // given
        Long targetSubId = 2L;
        given(familyApplyTargetJpaRepository.existsByTargetSubIdAndFamilyApplyStatus(targetSubId, ApplyStatus.PENDING))
                .willReturn(true);

        // when
        boolean result = familyApplyRepository.existsPendingApply(1L, targetSubId, 100L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공: 이미 대기 중인 신청이 있는지 확인한다 (존재하지 않음)")
    void existsPendingApplyFalse() {
        // given
        Long targetSubId = 2L;
        given(familyApplyTargetJpaRepository.existsByTargetSubIdAndFamilyApplyStatus(targetSubId, ApplyStatus.PENDING))
                .willReturn(false);

        // when
        boolean result = familyApplyRepository.existsPendingApply(1L, targetSubId, 100L);

        // then
        assertThat(result).isFalse();
    }
}
