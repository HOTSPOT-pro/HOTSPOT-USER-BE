package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;

/**
 * 관리자 정책 조회 서비스 단위 테스트 코드
 */

@ExtendWith(MockitoExtension.class)
class FindBlockPolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @InjectMocks
    private FindBlockPolicyServiceImpl findBlockPolicyService;

    @Test
    @DisplayName("정책 목록 조회 성공: 도메인을 Response DTO로 변환하여 반환한다")
    void findAllSuccess() {
        // given
        BlockPolicy domain = BlockPolicy.builder()
                .id(1L)
                .name("차단 정책")
                .policyType(PolicyType.SCHEDULED)
                .policySnapshot(new PolicySnapshot())
                .build();

        given(blockPolicyRepository.findAll()).willReturn(List.of(domain));

        // when
        List<BlockPolicyResponse> result = findBlockPolicyService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("차단 정책");
        assertThat(result.get(0).policyType()).isEqualTo(PolicyType.SCHEDULED);
    }
}
