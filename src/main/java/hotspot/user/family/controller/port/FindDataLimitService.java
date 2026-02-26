package hotspot.user.family.controller.port;

import hotspot.user.family.controller.response.FindDataLimitResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 특정 구성원의 데이터 한도 조회 서비스
 */
public interface FindDataLimitService {
    FindDataLimitResponse findDataLimit(Long targetSubId,
                                          Long requesterFamilyId,
                                          FamilyRole requesterRole);
}
