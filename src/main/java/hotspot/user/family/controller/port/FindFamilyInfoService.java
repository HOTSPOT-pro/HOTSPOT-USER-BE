package hotspot.user.family.controller.port;


import hotspot.user.family.controller.response.FamilyInfoResponse;

/**
 * 가족 정보 및 전체 구성원 정보 조회 서비스
 */
public interface FindFamilyInfoService {
    FamilyInfoResponse findFamilyInfoById(Long requesterMemberId, Long id);
}
