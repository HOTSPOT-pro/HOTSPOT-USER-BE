package hotspot.user.subscription.service.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.subscription.domain.Subscription;

/**
 * 회선 도메인에 저장하는 Repository
 */
public interface SubscriptionRepository {
    Optional<Subscription> findById(Long id);
    Optional<Subscription> findByMemberId(Long memberId);
    Optional<Subscription> findByPhoneHash(String phoneHash);
    Subscription save(Subscription subscription);
    Map<Long, DataPeriod> findDataPeriodsBySubIds(List<Long> subIds);
}
