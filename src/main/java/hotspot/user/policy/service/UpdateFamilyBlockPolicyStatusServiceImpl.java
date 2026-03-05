package hotspot.user.policy.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import hotspot.user.policy.controller.port.UpdateFamilyBlockPolicyStatusService;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

/**
 * 우리 가족이 생성한 정책의 상태 (비/활성화) 업데이트하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFamilyBlockPolicyStatusServiceImpl implements UpdateFamilyBlockPolicyStatusService {

    private final MemberRepository memberRepository;
    private final BlockPolicyRepository blockPolicyRepository;
    private final PolicySubRepository policySubRepository;

    @Override
    public UpdateFamilyBlockPolicyStatusResponse updateFamilyBlockPolicyStatus(
            UpdateFamilyBlockPolicyStatusRequest request,
            Long memberId,
            Long requesterFamilyId) {

        // 1. 요청자의 권한과 가족 소속을 검증한다. (OWNER만 가능)
        validateOwnerAuthority(memberId, requesterFamilyId);

        // 2. 해당 가족이 생성한 모든 정책을 조회한다.
        List<BlockPolicy> allFamilyPolicies = blockPolicyRepository.findAllByFamilyId(requesterFamilyId);

        // 3. 요청받은 ID 리스트가 모두 우리 가족의 정책인지 검증한다.
        List<Long> requestActiveIds = request.blockPolicyIdList();
        Set<Long> requestActiveIdsSet = new HashSet<>(requestActiveIds);
        validateAllPoliciesBelongToFamily(requestActiveIdsSet, allFamilyPolicies);

        // 4. 활성화할 ID 리스트와 비활성화할 ID 리스트를 분류한다.
        // 단일 순회 및 O(1) 조회로 최적화하기 위해 Map으로 변경
        Map<Boolean, List<BlockPolicy>> partitionedPolicies = allFamilyPolicies.stream()
                .collect(Collectors.partitioningBy(p -> requestActiveIdsSet.contains(p.getId())));

        List<Long> toActivate = partitionedPolicies.get(true).stream()
                .filter(p -> !p.isActive()) // 현재 꺼져있는 것만 켬
                .map(BlockPolicy::getId)
                .collect(Collectors.toList());

        List<Long> toDeactivate = partitionedPolicies.get(false).stream()
                .filter(BlockPolicy::isActive) // 현재 켜져있는 것만 끔
                .map(BlockPolicy::getId)
                .collect(Collectors.toList());

        // 5. 벌크 업데이트 수행
        if (!toActivate.isEmpty()) {
            blockPolicyRepository.bulkActivate(toActivate);
        }
        if (!toDeactivate.isEmpty()) {
            blockPolicyRepository.bulkDeActive(toDeactivate);
            // 정책이 비활성화되면 해당 정책을 적용 중인 모든 회선 매핑 정보도 비활성화 처리
            policySubRepository.bulkDeActiveByBlockPolicyIds(toDeactivate);
        }

        return UpdateFamilyBlockPolicyStatusResponse.builder()
                .familyId(requesterFamilyId)
                .blockedPolicyIdList(requestActiveIds)
                .build();
    }

    private void validateOwnerAuthority(Long memberId, Long requesterFamilyId) {
        // MemberDetailInfo를 통해 역할(Role)과 소속 가족(FamilyId)을 한 번에 확인
        MemberDetailInfo memberDetail = memberRepository.findDetailById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!Objects.equals(memberDetail.getFamilyId(), requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        if (memberDetail.getRole() != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
    }

    private void validateAllPoliciesBelongToFamily(Set<Long> requestIds, List<BlockPolicy> familyPolicies) {
        Set<Long> familyPolicyIds = familyPolicies.stream()
                .map(BlockPolicy::getId)
                .collect(Collectors.toSet());

        if (!familyPolicyIds.containsAll(requestIds)) {
            throw new ApplicationException(PolicyErrorCode.POLICY_ACCESS_DENIED);
        }
    }
}
