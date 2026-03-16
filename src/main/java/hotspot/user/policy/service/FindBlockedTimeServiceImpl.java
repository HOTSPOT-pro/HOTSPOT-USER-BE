package hotspot.user.policy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.controller.port.FindBlockedTimeService;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockedTimeResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.domain.BlockedTime;
import hotspot.user.policy.domain.mapper.BlockedTimeMapper;
import lombok.RequiredArgsConstructor;

/**
 * 구성원별 데이터 사용 차단 시간대를 조회하고 계산하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
public class FindBlockedTimeServiceImpl implements FindBlockedTimeService {

    private final FindMemberAppliedPolicyService findMemberAppliedPolicyService;
    private final FindFamilyAppliedPolicyService findFamilyAppliedPolicyService;

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
     * 
     * @param memberId 기준이 되는 구성원의 ID (가족 정보를 찾기 위해 사용)
     * @return 가족 구성원 각각의 병합된 차단 시간대 리스트
     */
    @Override
    @Transactional
    public List<BlockedTimeResponse> findFamilyBlockedTime(Long memberId) {
        // 1. 가족 전체 구성원의 적용 정책 리스트 조회 (memberId 기반 최신 소속 조회)
        FamilyAppliedPolicyResponse familyPolicyResponse = findFamilyAppliedPolicyService.findByMemberId(memberId);

        // 2. 각 구성원별로 적용된 정책 리스트를 순회하며 차단 시간대 계산
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
                .filter(p -> p.isActive())
                .forEach(p -> blockedTime.addPolicy(p.policySnapshot(), p.policyType()));

        // 3. 각 요일별로 겹치거나 맞닿아 있는 시간대들을 하나로 병합
        blockedTime.mergeAll();

        // 4. 도메인 객체를 최종 반환용 DTO로 변환
        return BlockedTimeMapper.toBlockedTimeResponse(policyResponse, blockedTime);
    }
}
