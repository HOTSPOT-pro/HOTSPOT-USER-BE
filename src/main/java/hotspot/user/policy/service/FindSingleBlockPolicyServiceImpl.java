package hotspot.user.policy.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.policy.controller.port.FindSingleBlockPolicyService;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 관리자 정책 조회 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSingleBlockPolicyServiceImpl implements FindSingleBlockPolicyService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final MemberRepository memberRepository;

    @Override
    public BlockPolicyResponse find(Long blockPolicyId, Long memberId, Long familyId) {
        // 1. OWNER 권한 및 가족 소속 검증
        validateOwnerAuthority(memberId, familyId);

        // 2. 정책 조회
        BlockPolicy blockPolicy = blockPolicyRepository.findByBlockPolicyId(blockPolicyId);

        // 3. 소유권 검증: 관리자 정책(null)이거나 본인 가족 정책이어야 함
        if (blockPolicy.getFamilyId() != null && !Objects.equals(blockPolicy.getFamilyId(), familyId)) {
            throw new ApplicationException(PolicyErrorCode.POLICY_ACCESS_DENIED);
        }

        return BlockPolicyMapper.toBlockPolicyResponse(blockPolicy);
    }

    private void validateOwnerAuthority(Long memberId, Long requesterFamilyId) {
        MemberDetailInfo memberDetail = memberRepository.findDetailByIdAndEmail(memberId, null)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!Objects.equals(memberDetail.getFamilyId(), requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        if (memberDetail.getRole() != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
    }
}
