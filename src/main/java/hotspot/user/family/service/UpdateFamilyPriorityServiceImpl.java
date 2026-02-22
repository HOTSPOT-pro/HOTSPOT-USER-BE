package hotspot.user.family.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.UpdateFamilyPriorityService;
import hotspot.user.family.controller.request.MemberPriorityRequest;
import hotspot.user.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.FamilySubscriptions;
import hotspot.user.family.domain.PriorityType;
import hotspot.user.family.domain.mapper.FamilyMapper;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성순위 값 업데이트 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFamilyPriorityServiceImpl implements UpdateFamilyPriorityService {

    private final FamilyRepository familyRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public UpdateFamilyPriorityResponse updateFamilyPriority(
            UpdateFamilyPriorityRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // 1. OWNER 권한 및 본인 가족 여부 검증
        validateAuthority(request, requesterFamilyId, requesterRole);

        // 2. 가족 도메인 조회 및 정책 타입 변경
        Family family = familyRepository.findById(request.familyId())
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        family.updatePriorityType(request.priorityType());
        familyRepository.save(family);

        // 3. 구성원 리스트 조회 및 일급 컬렉션 생성
        List<FamilySubscription> familySubList = familySubscriptionRepository.findByFamilyId(family.getId());
        FamilySubscriptions members = FamilySubscriptions.builder()
                .subscriptions(familySubList)
                .build();

        // 4. 도메인 로직 실행 (우선순위 동기화 및 꼼꼼한 비즈니스 규칙 검증)
        syncPriorities(members, request);

        // 5. 변경 사항 영속화 (UPDATE만)
        familySubscriptionRepository.updatePriorities(members.toList());

        // 6. 결과 반환 (DB 재조회 없이, 최신화된 메모리 도메인 객체를 바로 매퍼로 전달)
        return FamilyMapper.toUpdateFamilyPriorityResponse(family, members.toList());
    }

    private void validateAuthority(UpdateFamilyPriorityRequest request, Long requesterFamilyId, FamilyRole requesterRole) {
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
        if (!request.familyId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }
    }

    // 우선순위 타입에 따른 우선순위 업데이트
    private void syncPriorities(FamilySubscriptions members, UpdateFamilyPriorityRequest request) {
        if (request.priorityType() == PriorityType.FIFO) {
            members.updateAllToFifo();
        } else {
            // DTO 리스트를 Map으로 변환하여 도메인 객체에 전달
            Map<Long, Integer> priorityMap = request.memberPriorities().stream()
                    .collect(Collectors.toMap(
                            MemberPriorityRequest::subId,
                            MemberPriorityRequest::priority
                    ));
            members.updatePriorities(priorityMap);
        }
    }
}
