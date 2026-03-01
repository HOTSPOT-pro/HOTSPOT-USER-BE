package hotspot.user.policy.service.port;

import java.util.List;

import hotspot.user.policy.domain.BlockPolicy;

/**
 * 관리자 정책 리포지토리 (컨트롤러 - 서비스 구간)
 */
public interface BlockPolicyRepository {
    List<BlockPolicy> findAll();
    List<BlockPolicy> findAllById(List<Long> idList);
    List<BlockPolicy> findAllByFamilyId(Long familyId);
}
