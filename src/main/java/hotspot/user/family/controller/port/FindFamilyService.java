package hotspot.user.family.controller.port;

import hotspot.user.family.controller.response.FamilyResponse;

/**
 * 가족 정보 조회 서비스
 */
public interface FindFamilyService {
    FamilyResponse findById(Long id);
}
