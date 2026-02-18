package hotspot.user.family.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;

/**
 * 가족-회선 매핑 JPA 저장소
 */
public interface FamilySubscriptionJpaRepository extends JpaRepository<FamilySubscriptionEntity, Long> {

    @EntityGraph(attributePaths = {"family", "subscription"})
    Optional<FamilySubscriptionEntity> findBySubId(Long subscriptionId);
}
