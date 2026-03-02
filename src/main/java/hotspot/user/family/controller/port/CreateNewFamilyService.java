package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 구성원 추가 / 삭제 신청 서비스
 */
public interface CreateNewFamilyService {
    CreateNewFamilyResponse manage(
            Long requesterMemberId,
            Long familyId,
            FamilyRole requesterFamilyRole,
            CreateNewFamilyRequest request);
}
