package hotspot.user.policy.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.FindBlockedTimeService;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.BlockedTimeResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.domain.BlockedTime;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.domain.mapper.BlockedTimeMapper;
import lombok.RequiredArgsConstructor;

/**
 * 구성원별 데이터 사용 차단 시간대를 조회하고 계산하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class FindBlockedTimeServiceImpl implements FindBlockedTimeService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final FindMemberAppliedPolicyService findMemberAppliedPolicyService;
    private final FindFamilyAppliedPolicyService findFamilyAppliedPolicyService;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    /**
     * 특정 구성원의 차단 시간대 조회
     *
     * @param memberId 조회할 구성원의 ID
     * @return 요일별 병합된 차단 시간대 정보
     */
    @Override
    @Transactional
    public BlockedTimeResponse findMemberBlockedTime(Long memberId) {
        // 1. 해당 멤버에게 현재 적용된 모든 정책 리스트 조회 (만료 체크 포함)
        AppliedPolicyResponse policyResponse = findMemberAppliedPolicyService.findByMemberId(memberId);

        // 2. 조회된 정책들을 기반으로 실제 차단 시간 계산 및 병합
        return calculateBlockedTime(policyResponse);
    }

    /**
     * 사용자가 속한 가족 전체 구성원의 차단 시간대 조회
     * 요청자는 OWNER 또는 PARENT 권한을 가져야 합니다.
     *
     * @param memberId 기준이 되는 구성원의 ID (가족 정보를 찾기 위해 사용)
     * @return 가족 구성원 각각의 병합된 차단 시간대 리스트
     */
    @Override
    @Transactional
    public List<BlockedTimeResponse> findFamilyBlockedTime(Long memberId) {
        // 1. 요청자의 권한 확인 (OWNER 또는 PARENT만 가능)
        FamilySubscription requesterSub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (requesterSub.getFamilyRole() != FamilyRole.OWNER && requesterSub.getFamilyRole() != FamilyRole.PARENT) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        // 2. 가족 전체 구성원의 적용 정책 리스트 조회 (memberId 기반 최신 소속 조회)
        FamilyAppliedPolicyResponse familyPolicyResponse = findFamilyAppliedPolicyService.findByMemberId(memberId);

        // 3. 각 구성원별로 적용된 정책 리스트를 순회하며 차단 시간대 계산
        return familyPolicyResponse.memberPolicies().stream()
                .map(this::calculateBlockedTime)
                .collect(Collectors.toList());
    }

    /**
     * 특정 구성원의 정책 리스트(`AppliedPolicyResponse`)를 분석하여
     * 중복되거나 인접한 시간대를 병합한 최종 차단 시간대 DTO 생성
     *
     * @param policyResponse 적용된 정책 응답 객체
     * @return 병합된 차단 시간대 응답 객체
     */
    private BlockedTimeResponse calculateBlockedTime(AppliedPolicyResponse policyResponse) {
        // 1. 요일별 시간 구간을 관리할 도메인 객체 생성
        BlockedTime blockedTime = new BlockedTime();

        // 2. 활성화된 정책 스냅샷들을 도메인 객체에 추가 (자정 넘김 처리 포함)
        policyResponse.blockPolicyResponseList().stream()
                .filter(BlockPolicyResponse::isActive)
                .forEach(p -> {
                    if (p.policyType() == PolicyType.ONCE) {
                        PolicySnapshot graphSnapshot = createGraphSnapshotForOncePolicy(p);
                        // SCHEDULED로 처리하여 BlockedTime이 요일에 맞게 그리도록 유도
                        blockedTime.addPolicy(graphSnapshot, PolicyType.SCHEDULED);
                    } else {
                        blockedTime.addPolicy(p.policySnapshot(), p.policyType());
                    }
                });

        // 3. 각 요일별로 겹치거나 맞닿아 있는 시간대들을 하나로 병합
        blockedTime.mergeAll();

        // 4. 도메인 객체를 최종 반환용 DTO로 변환
        return BlockedTimeMapper.toBlockedTimeResponse(policyResponse, blockedTime);
    }

    /**
     * ONCE 정책인 경우 그래프 표시를 위해 실제 적용 시간(modifiedTime)을 기준으로 요일과 시간을 계산하여
     * 새로운 PolicySnapshot(그래프용 스냅샷)을 생성
     *
     * @param p ONCE 타입의 정책 응답 객체
     * @return 그래프 표시용 SCHEDULED 형태의 PolicySnapshot
     */
    private PolicySnapshot createGraphSnapshotForOncePolicy(BlockPolicyResponse p) {
        PolicySnapshot snapshot = p.policySnapshot();
        LocalTime start = snapshot.getStartTime() != null
                ? snapshot.getStartLocalTime()
                : p.modifiedTime().toLocalTime();

        int duration = snapshot.getDurationMinutes() != null ? snapshot.getDurationMinutes() : 0;
        LocalTime end = snapshot.getEndTime() != null
                ? snapshot.getEndLocalTime()
                : start.plusMinutes(duration);

        return PolicySnapshot.builder()
                .days(List.of(p.modifiedTime().getDayOfWeek()))
                .startTime(start.format(TIME_FORMATTER))
                .endTime(end.format(TIME_FORMATTER))
                .build();
    }
}
