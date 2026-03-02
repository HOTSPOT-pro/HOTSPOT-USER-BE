package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.infrastructure.entity.FamilyApplyEntity;
import hotspot.user.family.infrastructure.entity.FamilyApplyTargetEntity;
import hotspot.user.member.domain.FamilyRole;

@ExtendWith(MockitoExtension.class)
class FamilyApplyTargetRepositoryImplTest {

    @InjectMocks
    private FamilyApplyTargetRepositoryImpl familyApplyTargetRepository;

    @Mock
    private FamilyApplyTargetJpaRepository familyApplyTargetJpaRepository;

    @Test
    @DisplayName("성공: 여러 개의 가족 신청 타겟을 한꺼번에 저장한다.")
    void saveAllSuccess() {
        // given
        FamilyApplyTarget target1 = FamilyApplyTarget.builder()
                .familyApplyId(1L).targetSubId(10L).targetFamilyRole(FamilyRole.CHILD).build();
        FamilyApplyTarget target2 = FamilyApplyTarget.builder()
                .familyApplyId(1L).targetSubId(11L).targetFamilyRole(FamilyRole.PARENT).build();

        List<FamilyApplyTarget> targets = List.of(target1, target2);

        given(familyApplyTargetJpaRepository.saveAll(anyList())).willReturn(List.of(
                FamilyApplyTargetEntity.domainToEntity(target1),
                FamilyApplyTargetEntity.domainToEntity(target2)
        ));

        // when
        List<FamilyApplyTarget> savedTargets = familyApplyTargetRepository.saveAll(targets);

        // then
        assertThat(savedTargets).hasSize(2);
        assertThat(savedTargets.get(0).getTargetSubId()).isEqualTo(10L);
        assertThat(savedTargets.get(1).getTargetSubId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("성공: 특정 회선 ID 리스트로 대기 중인(PENDING) 신청 타겟을 조회한다.")
    void findAllPendingByTargetSubIdInSuccess() {
        // given
        List<Long> subIds = List.of(10L, 11L);

        // PENDING 상태인 부모 엔티티 설정
        FamilyApplyEntity pendingApply = FamilyApplyEntity.builder()
                .familyApplyId(1L)
                .status(ApplyStatus.PENDING)
                .build();

        FamilyApplyTargetEntity entity = FamilyApplyTargetEntity.builder()
                .familyApply(pendingApply)
                .targetSubId(10L)
                .build();

        given(familyApplyTargetJpaRepository.findAllByTargetSubIdInAndFamilyApplyStatus(subIds, ApplyStatus.PENDING))
                .willReturn(List.of(entity));

        // when
        List<FamilyApplyTarget> result = familyApplyTargetRepository.findAllPendingByTargetSubIdIn(subIds);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetSubId()).isEqualTo(10L);
    }
}
