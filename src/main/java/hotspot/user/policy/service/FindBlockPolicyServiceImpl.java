package hotspot.user.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.controller.port.FindBlockPolicyService;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 정책 조회 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindBlockPolicyServiceImpl implements FindBlockPolicyService {

    private final BlockPolicyRepository blockPolicyRepository;

    @Override
    public List<BlockPolicyResponse> findAll() {
        return blockPolicyRepository.findAll().stream()
                .map(BlockPolicyMapper::toBlockPolicyResponse)
                .toList();
    }
}
