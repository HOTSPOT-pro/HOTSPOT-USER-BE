package hotspot.user.family.service.port;


import java.util.List;

import hotspot.user.family.domain.FamilyApplyTarget;

/**
 * 가족 생성 / 구성원 추가 / 삭제 신청 타겟 repository
 */
public interface FamilyApplyTargetRepository {
    FamilyApplyTarget save(FamilyApplyTarget familyApplyTarget);
    List<FamilyApplyTarget> saveAll(List<FamilyApplyTarget> familyApplyTargetList);
    boolean existsPendingApplyByTargetSubId(Long targetSubId);
}
