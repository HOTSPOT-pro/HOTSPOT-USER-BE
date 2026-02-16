package hotspot.user.subscription.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

/**
 * 실제 회선 저장하는 Repository (DB)
 */
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findByMemberId(Long memberId);
}
