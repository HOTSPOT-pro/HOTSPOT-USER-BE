package hotspot.user.policy.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.policy.controller.port.FindBlockStatusService;
import hotspot.user.policy.controller.response.BlockedReasonResponse;
import hotspot.user.policy.controller.response.BlockedStatusResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 회선별 실시간 차단 여부 및 사유를 판단하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindBlockStatusServiceImpl implements FindBlockStatusService {

    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final PolicySubRepository policySubRepository;
    private final BlockPolicyRepository blockPolicyRepository;
    private final Clock clock;

    /**
     * 나의 상세 차단 상태를 조회한다.
     * 즉시 차단(isLocked) 여부와 현재 시간 기준 정책 차단 여부, 차단 사유(정책 리스트)를 포함
     *
     * @param memberId 내 memberId
     * @return 즉시 차단 여부, 현재 차단 여부, 차단 사유 리스트를 포함한 응답 DTO
     */
    @Override
    public BlockedStatusResponse findMyBlockStatus(Long memberId) {
        FamilySubscription familySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        Long subId = familySub.getSubscription().getId();

        // 1. 회선의 즉시 차단(isLocked) 여부 확인
        boolean isImmediateBlocked = Boolean.TRUE.equals(familySub.getSubscription().getIsLocked());

        // 2. 현재 시간 기준, 적용된 정책들에 의한 차단 여부 판단
        List<BlockPolicy> activePolicies = getActivePolicies(subId);
        LocalDateTime now = LocalDateTime.now(clock);

        List<BlockedReasonResponse> blockingReasons = new ArrayList<>();
        for (BlockPolicy policy : activePolicies) {
            if (isTimePolicyBlockingNow(policy, now)) {
                blockingReasons.add(BlockedReasonResponse.builder()
                        .id(policy.getId())
                        .name(policy.getName())
                        .build());
            }
        }

        return BlockedStatusResponse.builder()
                .isImmediateBlocked(isImmediateBlocked)
                .isCurrentlyBlocked(isImmediateBlocked || !blockingReasons.isEmpty())
                .blockedPolicies(blockingReasons)
                .build();
    }

    /**
     * 특정 회선(Subscription)이 현재 차단 상태인지 여부만 확인한다. (가족 구성원 리스트 등에서 사용)
     *
     * @param subId 조회할 회선 ID
     * @return 즉시 차단 또는 정책 차단 중 하나라도 해당되면 true
     */
    @Override
    public boolean isCurrentlyBlocked(Long subId) {
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 즉시 차단 상태면 바로 true 반환
        if (Boolean.TRUE.equals(familySub.getSubscription().getIsLocked())) {
            return true;
        }

        // 적용된 활성 정책 중 현재 시간을 차단하는 정책이 있는지 확인
        List<BlockPolicy> activePolicies = getActivePolicies(subId);
        LocalDateTime now = LocalDateTime.now(clock);

        return activePolicies.stream()
                .anyMatch(policy -> isTimePolicyBlockingNow(policy, now));
    }

    /**
     * 해당 회선에 적용되어 있는 활성(Active) 정책 리스트를 조회한다.
     */
    private List<BlockPolicy> getActivePolicies(Long subId) {
        List<PolicySub> policySubs = policySubRepository.findActiveBySubId(subId);
        List<Long> policyIds = policySubs.stream().map(PolicySub::getBlockPolicyId).toList();
        return blockPolicyRepository.findAllById(policyIds);
    }

    /**
     * 개별 정책이 현재 시간(now) 기준으로 차단을 수행해야 하는지 판단한다.
     *
     * @param policy 검사할 정책 도메인
     * @param now 기준 시간 (테스트 용이성을 위해 파라미터로 받음)
     * @return 현재 차단 중이면 true
     */
    private boolean isTimePolicyBlockingNow(BlockPolicy policy, LocalDateTime now) {
        // 정책 자체가 비활성화 상태면 차단하지 않음
        if (!policy.isActive()) return false;

        // 일회성(ONCE) 정책은 활성화되어 있다는 것 자체가 현재 차단 중임을 의미함 (만료 시 비활성화되므로)
        if (policy.getPolicyType() == PolicyType.ONCE) return true;

        // 반복(SCHEDULED) 정책 시간 및 요일 체크
        PolicySnapshot snapshot = policy.getPolicySnapshot();
        LocalTime start = snapshot.getStartLocalTime();
        LocalTime end = snapshot.getEndLocalTime();
        if (start == null || end == null) return false;

        LocalTime currentTime = now.toLocalTime();
        DayOfWeek today = now.getDayOfWeek();
        DayOfWeek yesterday = today.minus(1);

        // 1. 24시간 내내 차단 (시작 시간과 종료 시간이 같을 경우 해당 요일이면 항상 차단)
        if (start.equals(end)) {
            return snapshot.getDays().contains(today);
        }

        boolean overnight = start.isAfter(end);
        if (!overnight) {
            // 2. 일반적인 시간 범위 (예: 09:00 ~ 18:00)
            return !currentTime.isBefore(start) && currentTime.isBefore(end)
                    && snapshot.getDays().contains(today);
        }

        // 3. 익일 종료 시간 범위 (예: 22:00 ~ 06:00)
        // 오늘 밤에 시작된 경우 (22:00 ~ 23:59:59) -> 오늘 요일 체크
        if (!currentTime.isBefore(start)) {
            return snapshot.getDays().contains(today);
        }
        // 어제 밤부터 이어진 오늘 새벽인 경우 (00:00 ~ 05:59:59) -> 어제 요일 체크
        if (currentTime.isBefore(end)) {
            return snapshot.getDays().contains(yesterday);
        }

        return false;
    }
}
