package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.domain.PriorityType;
import hotspot.user.family.infrastructure.entity.FamilyDetailInfoDto;
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import hotspot.user.member.infrastructure.entity.MemberEntity;

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

    @Test
    @DisplayName("가족 정보 저장 성공")
    void saveSuccess() {
        // given
        Family family = Family.builder()
                .id(1L)
                .familyNum(4)
                .familyDataAmount(10000)
                .priorityType(PriorityType.PRIORITY)
                .build();

        FamilyEntity entity = FamilyEntity.builder()
                .familyId(1L)
                .familyNum(4)
                .familyDataAmount(10000)
                .priorityType(PriorityType.PRIORITY)
                .build();

        given(familyJpaRepository.save(ArgumentMatchers.any(FamilyEntity.class))).willReturn(entity);

        // when
        Family result = familyRepository.save(family);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPriorityType()).isEqualTo(PriorityType.PRIORITY);
    }

    @Test
    @DisplayName("가족 상세 정보 통합 조회 성공")
    void findInfoByIdSuccess() {
        // given
        Long familyId = 1L;
        FamilyEntity familyEntity = FamilyEntity.builder()
                .familyId(familyId)
                .familyNum(2)
                .familyDataAmount(5000)
                .build();

        MemberEntity memberEntity = MemberEntity.builder()
                .id(10L)
                .name("멤버1")
                .status(Status.APPROVED)
                .build();

        FamilyDetailInfoDto dto = FamilyDetailInfoDto.builder()
                .familyEntity(familyEntity)
                .memberEntity(memberEntity)
                .email("test@email.com")
                .phone("010-1111-2222")
                .subId(100L)
                .role(FamilyRole.OWNER)
                .build();

        given(familyJpaRepository.findFamilyDetailQueryResult(familyId)).willReturn(List.of(dto));

        // when
        Optional<FamilyDetailInfo> result = familyRepository.findInfoById(familyId);

        // then
        assertThat(result).isPresent();
        FamilyDetailInfo info = result.get();
        assertThat(info.getFamilyId()).isEqualTo(familyId);
        assertThat(info.getFamilyNum()).isEqualTo(2);
        assertThat(info.getMemberDetailInfoList()).hasSize(1);
        assertThat(info.getMemberDetailInfoList().get(0).getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("가족 상세 정보 조회 실패: 결과가 없는 경우")
    void findInfoByIdNotFound() {
        // given
        Long familyId = 999L;
        given(familyJpaRepository.findFamilyDetailQueryResult(familyId)).willReturn(List.of());

        // when
        Optional<FamilyDetailInfo> result = familyRepository.findInfoById(familyId);

        // then
        assertThat(result).isEmpty();
    }
}
