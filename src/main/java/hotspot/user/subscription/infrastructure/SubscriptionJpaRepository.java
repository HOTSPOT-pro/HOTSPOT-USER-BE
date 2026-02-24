package hotspot.user.subscription.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

/**
 * 실제 회선 저장하는 Repository (DB)
 */
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {

    @EntityGraph(attributePaths = {"plan"})
    Optional<SubscriptionEntity> findByMemberId(Long memberId);

    Optional<SubscriptionEntity> findByPhoneHash(String phoneHash);

    @Query("""
        select s
        from SubscriptionEntity s
        join fetch s.plan
        where s.subId in :subIds
    """)
    List<SubscriptionEntity> findAllByIdIn(List<Long> subIds);
}
