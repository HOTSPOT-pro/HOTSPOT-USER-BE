package hotspot.user.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.FindFamilyBlockPolicyService;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

/**
 * 우리 가족이 생성한 정책 조회 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyBlockPolicyServiceImpl implements FindFamilyBlockPolicyService {

    private final BlockPolicyRepository blockPolicyRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public List<BlockPolicyResponse> findAllByFamilyId(Long memberId, Long familyId) {

        // DB에서 최신 가족 매핑 정보 조회
        // 조회자가 가족 구성원에 소속되어있지 않으면 예외 처리
        FamilySubscription familySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 실제 구성된 familyId 일치 여부 확인
        if (!familySub.getFamily().getId().equals(familyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // OWNER or PARENT인지 확인
        if (familySub.getFamilyRole() != FamilyRole.OWNER && familySub.getFamilyRole() != FamilyRole.PARENT) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        return blockPolicyRepository.findAllByFamilyId(familyId).stream()
                .map(BlockPolicyMapper::toBlockPolicyResponse)
                .toList();
    }
}
