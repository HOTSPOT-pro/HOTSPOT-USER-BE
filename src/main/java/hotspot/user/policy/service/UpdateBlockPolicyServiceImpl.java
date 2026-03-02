package hotspot.user.policy.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.policy.controller.port.UpdateBlockPolicyService;
import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 정책 조회 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateBlockPolicyServiceImpl implements UpdateBlockPolicyService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public BlockPolicyResponse update(BlockPolicyRequest request, Long blockPolicyId, Long memberId, Long familyId) {
        // 1. OWNER 권한 및 가족 소속 검증
        validateOwnerAuthority(memberId, familyId);

        // 2. 기존 정책 조회
        BlockPolicy blockPolicy = blockPolicyRepository.findByBlockPolicyId(blockPolicyId);

        // 3. 소유권 검증: 본인 가족 정책만 수정 가능 (관리자 정책 수정 불가)
        if (blockPolicy.getFamilyId() == null || !Objects.equals(blockPolicy.getFamilyId(), familyId)) {
            throw new ApplicationException(PolicyErrorCode.POLICY_ACCESS_DENIED);
        }

        // 4. 정책 스냅샷 유효성 검증 (값이 있을 때만 수행)
        if (request.policySnapshot() != null) {
            PolicyType type = request.policyType() != null ? request.policyType() : blockPolicy.getPolicyType();
            request.policySnapshot().validate(type);
        }

        // 5. 도메인 객체 업데이트 및 저장
        BlockPolicy updated = blockPolicy.update(
                request.name(),
                request.policyDescription(),
                request.policyType(),
                request.policySnapshot(),
                request.isActive()
        );

        BlockPolicy saved = blockPolicyRepository.save(updated);

        return BlockPolicyMapper.toBlockPolicyResponse(saved);
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
