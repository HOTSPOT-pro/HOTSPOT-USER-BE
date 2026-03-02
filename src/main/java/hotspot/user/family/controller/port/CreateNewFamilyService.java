package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 신규 생성 서비스
 */
public interface CreateNewFamilyService {
    CreateNewFamilyResponse createNewFamily(
            Long requesterMemberId,
            CreateNewFamilyRequest request);
}
