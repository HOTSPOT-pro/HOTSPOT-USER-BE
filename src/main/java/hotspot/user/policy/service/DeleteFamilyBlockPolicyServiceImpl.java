package hotspot.user.policy.service;

import java.util.List;
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
import hotspot.user.policy.controller.port.DeleteFamilyBlockPolicyService;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.policy.service.util.PolicyDeletedOutboxPublisher;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class DeleteFamilyBlockPolicyServiceImpl implements DeleteFamilyBlockPolicyService {
    private final MemberRepository memberRepository;
    private final BlockPolicyRepository blockPolicyRepository;
    private final PolicySubRepository policySubRepository;
    private final PolicyDeletedOutboxPublisher policyDeletedOutboxPublisher;

    @Override
    public void delete(List<Long> policyIdList, Long memberId, Long familyId) {
        // 1. 요청자의 OWNER 권한 및 가족 소속 검증
        validateOwnerAuthority(memberId, familyId);

        // 2. 삭제 대상 정책들이 실제로 해당 가족의 소유인지 검증
        List<BlockPolicy> targetPolicies = blockPolicyRepository.findAllById(policyIdList);
        validatePolicyOwnership(policyIdList, targetPolicies, familyId);

        // Outbox publish
        policyDeletedOutboxPublisher.publishAll(policyIdList, familyId);

        // 3. 연관 데이터(policy_sub) 처리: 정책이 삭제되므로 적용 중인 회선에서도 비활성화
        policySubRepository.bulkDeActiveByBlockPolicyIds(policyIdList);

        // 4. 정책 벌크 삭제 (Soft Delete) 수행
        blockPolicyRepository.bulkDelete(policyIdList);
    }

    // 요청자가 OWNER인지 권한 검증
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

    // 정책이 존재하는지 & 우리 가족의 정책인지 확인
    private void validatePolicyOwnership(List<Long> requestIds, List<BlockPolicy> targetPolicies, Long familyId) {
        if (targetPolicies.size() != requestIds.size()) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }

        boolean hasAnotherFamilyPolicy = targetPolicies.stream()
                .anyMatch(p -> !Objects.equals(p.getFamilyId(), familyId));

        if (hasAnotherFamilyPolicy) {
            throw new ApplicationException(PolicyErrorCode.POLICY_ACCESS_DENIED);
        }
    }
}
