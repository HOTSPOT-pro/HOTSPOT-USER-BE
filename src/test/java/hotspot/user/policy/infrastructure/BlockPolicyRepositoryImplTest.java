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
    @DisplayName("전체 정책 목록 조회 성공: 엔티티를 도메인 객체(Snapshot 포함)로 변환한다")
    void findAllSuccess() {
        // given
        PolicySnapshot snapshot = new PolicySnapshot(); // 실제 필드에 맞게 인스턴스화 필요
        BlockPolicyEntity entity = BlockPolicyEntity.builder()
                .blockPolicyId(1L)
                .policyName("기본 차단")
                .policyType(PolicyType.SCHEDULED)
                .dateSnapshot(snapshot)
                .build();

        given(blockPolicyJpaRepository.findAll()).willReturn(List.of(entity));

        // when
        List<BlockPolicy> result = blockPolicyRepository.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("기본 차단");
        assertThat(result.get(0).getPolicyType()).isEqualTo(PolicyType.SCHEDULED);
    }
}
