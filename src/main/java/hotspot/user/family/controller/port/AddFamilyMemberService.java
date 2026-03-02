package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 구성원 추가 서비스
 */
public interface AddFamilyMemberService {
    AddFamilyMemberResponse addFamilyMember(
            Long requesterMemberId,
            Long familyId,
            AddFamilyMemberRequest request);
}
