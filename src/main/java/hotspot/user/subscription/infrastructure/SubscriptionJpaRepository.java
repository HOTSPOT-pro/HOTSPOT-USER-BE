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

    @EntityGraph(attributePaths = {"plan", "member"})
    @Override
    Optional<SubscriptionEntity> findById(Long id);

    @EntityGraph(attributePaths = {"plan", "member"})
    Optional<SubscriptionEntity> findByMemberId(Long memberId);

    @EntityGraph(attributePaths = {"plan", "member"})
    Optional<SubscriptionEntity> findByPhoneHash(String phoneHash);

    @Query("""
        select s
        from SubscriptionEntity s
        join fetch s.plan
        join fetch s.member
        where s.subId in :subIds
    """)
    List<SubscriptionEntity> findAllByIdIn(List<Long> subIds);
}
