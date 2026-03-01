package hotspot.user.policy.infrastructure;

import java.util.List;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;
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
        return blockPolicyJpaRepository.findAllByIsActiveTrueAndFamilyIdIsNull().stream()
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

    // familyId에 해당하는 모든 정책 리턴
    @Override
    public List<BlockPolicy> findAllByFamilyId(Long familyId) {
        return blockPolicyJpaRepository.findAllByFamilyId(familyId).stream()
                .map(BlockPolicyEntity::entityToDomain)
                .toList();
    }

    @Override
    public void bulkActivate(List<Long> ids) {
        blockPolicyJpaRepository.bulkActivate(ids);
    }

    @Override
    public void bulkDeActive(List<Long> ids) {
        blockPolicyJpaRepository.bulkDeActive(ids);
    }

    // 가족 정책 삭제
    @Override
    public void bulkDelete(List<Long> ids) {
        blockPolicyJpaRepository.bulkDelete(ids);
    }

    // 단일 정책 조회
    @Override
    public BlockPolicy findByBlockPolicyId(Long blockPolicyId) {
        BlockPolicyEntity blockPolicyEntity = blockPolicyJpaRepository.findByBlockPolicyId(blockPolicyId)
                .orElseThrow(() -> new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND));

        return blockPolicyEntity.entityToDomain();
    }
}
