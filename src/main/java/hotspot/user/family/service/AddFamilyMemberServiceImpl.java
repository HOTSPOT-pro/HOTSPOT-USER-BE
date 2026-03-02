package hotspot.user.family.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.controller.port.AddFamilyMemberService;
import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성원 신청 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional
public class AddFamilyMemberServiceImpl implements AddFamilyMemberService {

    @Override
    public AddFamilyMemberResponse addFamilyMember(
            Long requesterMemberId,
            Long familyId,
            AddFamilyMemberRequest request) {

        // 1. 신청자의 역할이 OWNER인지 확인

        // 2. request를 순회하며 입력된 전화번호가 가입된 회선인지 확인

        // 3. 해당 회선이 현재 아무 가족에 속해있지 않은지 확인

        // 4. 타입별 비즈니스 규칙 검증
        // 4-1. 서류 URL이 있는지 확인
        // 4-2. 구성원별로 역할이 부여됐는지 확인

        // 5. 이미 처리 대기 중인 동일한 신청이 있는지 확인

        return null;
    }
}
