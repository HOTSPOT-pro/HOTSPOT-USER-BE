package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlockPolicyRepositoryImpl implements BlockPolicyRepository {
    private final BlockPolicyJpaRepository blockPolicyJpaRepository;

    @Override
    public List<BlockPolicy> findAll() {
        return blockPolicyJpaRepository.findAll().stream()
                .map(BlockPolicyEntity::entityToDomain)
                .toList();
    }

    // policyId에 해당하는 모든 정책 리턴
    @Override
    public List<BlockPolicy> findAllById(List<Long> idList) {
        return blockPolicyJpaRepository.findAllById(idList).stream()
                .map(BlockPolicyEntity::entityToDomain)
                .toList();
    }
}
