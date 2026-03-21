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

    // 회선 id 목록 받아서 해당하는 FamilySubscription 리스트 리턴
    List<FamilySubscription> findAllBySubIdIn(List<Long> subIds);
    List<FamilySubscription> findByFamilyId(Long familyId);
    Optional<FamilySubscription> findByMemberId(Long memberId);
    FamilySubscription save(FamilySubscription familySubscription);
    void updatePriorities(List<FamilySubscription> subscriptions);
    void updateDataLimit(Long subId, long dataLimit);
    FamilySubDataLimit findDataLimitBySubId(Long subId); // subId로 데이터 한도 관련 정보 찾기
}
