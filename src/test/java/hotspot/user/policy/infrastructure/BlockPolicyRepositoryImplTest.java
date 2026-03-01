package hotspot.user.policy.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;

/**
 * 관리자 정책 Repository 조회 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class BlockPolicyRepositoryImplTest {

    @Mock
    private BlockPolicyJpaRepository blockPolicyJpaRepository;

    @InjectMocks
    private BlockPolicyRepositoryImpl blockPolicyRepository;

    @Test
    @DisplayName("전체 정책 목록 조회 성공: 활성 상태의 시스템 관리자 정책(familyId=null)만 조회한다")
    void findAllSuccess() {
        // given
        PolicySnapshot snapshot = new PolicySnapshot();
        BlockPolicyEntity entity = BlockPolicyEntity.builder()
                .blockPolicyId(1L)
                .policyName("기본 차단")
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(snapshot)
                .isActive(true)
                .familyId(null)
                .build();

        given(blockPolicyJpaRepository.findAllByIsActiveTrueAndFamilyIdIsNull()).willReturn(List.of(entity));

        // when
        List<BlockPolicy> result = blockPolicyRepository.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("기본 차단");
        assertThat(result.get(0).getFamilyId()).isNull();
    }

    @Test
    @DisplayName("성공: ID 리스트로 정책들을 일괄 조회한다")
    void findAllByIdSuccess() {
        // given
        BlockPolicyEntity entity1 = BlockPolicyEntity.builder().blockPolicyId(1L).policyName("P1").build();
        BlockPolicyEntity entity2 = BlockPolicyEntity.builder().blockPolicyId(2L).policyName("P2").build();
        given(blockPolicyJpaRepository.findAllById(anyList())).willReturn(List.of(entity1, entity2));

        // when
        List<BlockPolicy> result = blockPolicyRepository.findAllById(List.of(1L, 2L));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공: 특정 가족 ID로 해당 가족이 생성한 정책 목록을 조회한다")
    void findAllByFamilyIdSuccess() {
        // given
        Long familyId = 100L;
        BlockPolicyEntity entity = BlockPolicyEntity.builder()
                .blockPolicyId(10L)
                .policyName("우리가족 정책")
                .familyId(familyId)
                .build();

        given(blockPolicyJpaRepository.findAllByFamilyId(familyId)).willReturn(List.of(entity));

        // when
        List<BlockPolicy> result = blockPolicyRepository.findAllByFamilyId(familyId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFamilyId()).isEqualTo(familyId);
        assertThat(result.get(0).getName()).isEqualTo("우리가족 정책");
    }

    @Test
    @DisplayName("성공: 정책들의 활성화 상태를 벌크 업데이트한다")
    void bulkActivateSuccess() {
        // given
        List<Long> ids = List.of(1L, 2L);

        // when
        blockPolicyRepository.bulkActivate(ids);

        // then
        verify(blockPolicyJpaRepository).bulkActivate(ids);
    }

    @Test
    @DisplayName("성공: 정책들의 비활성화 상태를 벌크 업데이트한다")
    void bulkDeActiveSuccess() {
        // given
        List<Long> ids = List.of(1L, 2L);

        // when
        blockPolicyRepository.bulkDeActive(ids);

        // then
        verify(blockPolicyJpaRepository).bulkDeActive(ids);
    }

    @Test
    @DisplayName("성공: 정책들을 일괄 삭제(Soft Delete)한다")
    void bulkDeleteSuccess() {
        // given
        List<Long> ids = List.of(1L, 2L);

        // when
        blockPolicyRepository.bulkDelete(ids);

        // then
        verify(blockPolicyJpaRepository).bulkDelete(ids);
    }

    @Test
    @DisplayName("성공: 새로운 정책을 저장한다")
    void saveSuccess() {
        // given
        BlockPolicy domain = BlockPolicy.builder().name("새 정책").build();
        BlockPolicyEntity entity = BlockPolicyEntity.builder().blockPolicyId(1L).policyName("새 정책").build();
        given(blockPolicyJpaRepository.save(any(BlockPolicyEntity.class))).willReturn(entity);

        // when
        BlockPolicy result = blockPolicyRepository.save(domain);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("새 정책");
    }

    @Test
    @DisplayName("성공: ID로 단일 정책을 조회한다")
    void findByBlockPolicyIdSuccess() {
        // given
        Long id = 1L;
        BlockPolicyEntity entity = BlockPolicyEntity.builder().blockPolicyId(id).policyName("정책").build();
        given(blockPolicyJpaRepository.findByBlockPolicyId(id)).willReturn(Optional.of(entity));

        // when
        BlockPolicy result = blockPolicyRepository.findByBlockPolicyId(id);

        // then
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 예외가 발생한다")
    void findByBlockPolicyIdFail() {
        // given
        given(blockPolicyJpaRepository.findByBlockPolicyId(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> blockPolicyRepository.findByBlockPolicyId(1L))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_NOT_FOUND);
    }
}
