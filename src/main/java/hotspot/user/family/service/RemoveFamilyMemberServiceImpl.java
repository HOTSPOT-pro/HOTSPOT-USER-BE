package hotspot.user.family.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.RemoveFamilyMemberService;
import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyRemoveSchedule;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.RemoveFamilyMemberMapper;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyRemoveScheduleRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성원 삭제 신청 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RemoveFamilyMemberServiceImpl implements RemoveFamilyMemberService {

    private final FamilyApplyRepository familyApplyRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FamilyRemoveScheduleRepository familyRemoveScheduleRepository;

    @Override
    public RemoveFamilyMemberResponse removeFamilyMember(Long requesterMemberId, RemoveFamilyMemberRequest request) {

        // 1. 요청자 권한 및 소속 확인 (방장 여부)
        FamilySubscription requesterFs = familySubscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (requesterFs.getFamilyRole() != FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);
        }

        Long familyId = requesterFs.getFamily().getId();
        
        // 2. 요청 리스트 중복 제거 및 데이터 준비
        List<Long> targetSubIds = request.targetSubIdList().stream()
                .distinct()
                .toList();

        // 3. 삭제 대상자들의 가족 소속 확인 및 중복 신청 여부 확인 (N+1 방지)
        List<FamilySubscription> targetFsList = familySubscriptionRepository.findAllBySubIdIn(targetSubIds);
        
        // 회선 존재 여부 검증 (요청한 ID들이 모두 가족 매핑 테이블에 존재하는지 확인)
        if (targetFsList.size() != targetSubIds.size()) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
        }

        // 해당 가족에 속한 subId Set 생성
        Set<Long> validFamilySubIds = targetFsList.stream()
                .filter(fs -> fs.getFamily().getId().equals(familyId))
                .map(fs -> fs.getSubscription().getId())
                .collect(Collectors.toSet());

        // 이미 삭제 스케줄이 잡혀있는 subId Set 생성 (중복 신청 방지)
        Set<Long> alreadyScheduledSubIds = familyRemoveScheduleRepository.findAllByTargetSubIdInAndStatus(targetSubIds, DeleteStatus.SCHEDULED)
                .stream()
                .map(FamilyRemoveSchedule::getTargetSubId)
                .collect(Collectors.toSet());

        for (Long targetSubId : targetSubIds) {

            // 같은 가족 소속인지 확인 (가족이 아닌 사람을 삭제하려 할 경우 차단)
            if (!validFamilySubIds.contains(targetSubId)) {
                throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
            }

            // 이미 처리 대기 중인 신청이 있는지 확인
            if (alreadyScheduledSubIds.contains(targetSubId)) {
                throw new ApplicationException(FamilyErrorCode.DUPLICATE_FAMILY_APPLY);
            }
        }

        // 4. 신청서 저장
        FamilyApply apply = RemoveFamilyMemberMapper.toFamilyApply(requesterFs.getSubscription().getId(), familyId);
        FamilyApply savedApply = familyApplyRepository.save(apply);

        // 5. 삭제 스케줄 생성 및 저장 - 다음 달 1일로 설정
        LocalDate scheduleDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        
        List<FamilyRemoveSchedule> schedules = targetSubIds.stream()
                .map(subId -> RemoveFamilyMemberMapper.toFamilyRemoveSchedule(familyId, subId, scheduleDate))
                .toList();

        List<FamilyRemoveSchedule> savedSchedules = familyRemoveScheduleRepository.saveAll(schedules);

        // 6. 응답 변환
        return RemoveFamilyMemberMapper.toRemoveFamilyMemberResponse(savedApply, savedSchedules);
    }
}
