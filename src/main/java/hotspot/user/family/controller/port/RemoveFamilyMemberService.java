package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;

/**
 * 가족 구성원 삭제 신청 서비스
 */
public interface RemoveFamilyMemberService {
    RemoveFamilyMemberResponse removeFamilyMember(Long requesterMemberId, RemoveFamilyMemberRequest request);
}
