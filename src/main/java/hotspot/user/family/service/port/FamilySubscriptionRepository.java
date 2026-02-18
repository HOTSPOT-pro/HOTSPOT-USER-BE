package hotspot.user.family.service.port;

import java.util.Optional;

import hotspot.user.family.domain.FamilySubscription;

/**
 * 가족-회선 매핑 저장소 포트
 */
public interface FamilySubscriptionRepository {
    Optional<FamilySubscription> findBySubId(Long subId);
}
