package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import hotspot.user.member.domain.FamilyRole;

@ExtendWith(MockitoExtension.class)
class FamilyApplyRepositoryImplTest {

    @Mock
    private FamilyApplyJpaRepository familyApplyJpaRepository;

    @InjectMocks
    private FamilyApplyRepositoryImpl familyApplyRepository;

    @Test
    @DisplayName("성공: 도메인 객체를 저장하고 다시 도메인으로 반환한다")
    void saveSuccess() {
        // given
        FamilyApply domain = FamilyApply.builder()
                .requesterSubId(1L).targetSubId(2L).familyId(100L)
                .applyType(ApplyType.ADD).targetFamilyRole(FamilyRole.CHILD).build();

        FamilyApplyEntity entity = FamilyApplyEntity.domainToEntity(domain);
        given(familyApplyJpaRepository.save(any(FamilyApplyEntity.class))).willReturn(entity);

        // when
        FamilyApply result = familyApplyRepository.save(domain);

        // then
        assertThat(result.getRequesterSubId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공: 대기 중인 신청이 존재하면 true를 반환한다")
    void existsPendingApplyTrue() {
        // given
        Long requesterSubId = 1L;
        Long targetSubId = 2L;
        Long familyId = 100L;

        given(familyApplyJpaRepository
                .existsByRequesterSubscriptionSubIdAndTargetSubscriptionSubIdAndFamilyFamilyIdAndStatus(
                eq(requesterSubId), eq(targetSubId), eq(familyId), eq(ApplyStatus.PENDING)))
                .willReturn(true);

        // when
        boolean result = familyApplyRepository.existsPendingApply(requesterSubId, targetSubId, familyId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공: 대기 중인 신청이 존재하지 않으면 false를 반환한다")
    void existsPendingApplyFalse() {
        // given
        Long requesterSubId = 1L;
        Long targetSubId = 2L;
        Long familyId = 100L;

        given(familyApplyJpaRepository
                .existsByRequesterSubscriptionSubIdAndTargetSubscriptionSubIdAndFamilyFamilyIdAndStatus(
                eq(requesterSubId), eq(targetSubId), eq(familyId), eq(ApplyStatus.PENDING)))
                .willReturn(false);

        // when
        boolean result = familyApplyRepository.existsPendingApply(requesterSubId, targetSubId, familyId);

        // then
        assertThat(result).isFalse();
    }
}
