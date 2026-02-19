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
}
