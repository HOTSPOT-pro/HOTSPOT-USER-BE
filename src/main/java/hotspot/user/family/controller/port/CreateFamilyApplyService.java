package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 구성원 추가 / 삭제 신청 서비스
 */
public interface CreateFamilyApplyService {
    CreateFamilyApplyResponse manage(
            Long requesterMemberId,
            Long familyId,
            FamilyRole requesterFamilyRole,
            CreateFamilyApplyRequest request);
}
