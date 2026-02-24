package hotspot.user.subscription.service.port;

import java.util.Optional;

import hotspot.user.subscription.domain.Subscription;

/**
 * 회선 도메인에 저장하는 Repository
 */
public interface SubscriptionRepository {
    Optional<Subscription> findById(Long id);
    Optional<Subscription> findByMemberId(Long memberId);
    Optional<Subscription> findByPhoneNumber(String phoneNumber);
    Subscription save(Subscription subscription);
}
