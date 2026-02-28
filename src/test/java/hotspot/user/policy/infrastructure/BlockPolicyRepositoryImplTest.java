package hotspot.user.policy.infrastructure;

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
}
