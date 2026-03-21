package hotspot.user.policy.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.policy.controller.port.CreateBlockPolicyService;
import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

/**
 * 정책 생성 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateBlockPolicyServiceImpl implements CreateBlockPolicyService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final MemberRepository memberRepository;


    @Override
    @Transactional
    public BlockPolicyResponse create(BlockPolicyRequest request, Long memberId, Long familyId) {
        // 1. OWNER 권한 및 가족 소속 검증
        validateOwnerAuthority(memberId, familyId);

        // 2. 정책 스냅샷 유효성 검증
        request.policySnapshot().validate(request.policyType());

        BlockPolicy blockPolicy = BlockPolicyMapper.toBlockPolicy(request, familyId);

        // 3. 정책 생성
        BlockPolicy saved = blockPolicyRepository.save(blockPolicy);

        return BlockPolicyMapper.toBlockPolicyResponse(saved);
    }

    private void validateOwnerAuthority(Long memberId, Long requesterFamilyId) {
        MemberDetailInfo memberDetail = memberRepository.findDetailById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!Objects.equals(memberDetail.getFamilyId(), requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        if (memberDetail.getRole() != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
    }
}
