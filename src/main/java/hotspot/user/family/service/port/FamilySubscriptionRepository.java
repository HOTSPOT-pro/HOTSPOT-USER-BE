package hotspot.user.family.service.port;

import java.util.List;
import java.util.Optional;

import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;

/**
 * 가족-회선 매핑 저장소 포트
 */
public interface FamilySubscriptionRepository {
    Optional<FamilySubscription> findBySubId(Long subId);
    List<FamilySubscription> findByFamilyId(Long familyId);
    Optional<FamilySubscription> findByMemberId(Long memberId);
    FamilySubscription save(FamilySubscription familySubscription);
    void updatePriorities(List<FamilySubscription> subscriptions);
    FamilySubDataLimit findDataLimitBySubId(Long subId); // subId로 데이터 한도 관련 정보 찾기
}
