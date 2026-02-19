package hotspot.user.family.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;

/**
 * 가족-회선 매핑 JPA 저장소
 */
public interface FamilySubscriptionJpaRepository extends JpaRepository<FamilySubscriptionEntity, Long> {

    @EntityGraph(attributePaths = {"family", "subscription", "subscription.member"})
    Optional<FamilySubscriptionEntity> findBySubscriptionSubId(Long subId);

    @EntityGraph(attributePaths = {"family", "subscription", "subscription.member"})
    List<FamilySubscriptionEntity> findByFamilyFamilyId(Long familyId);

    @EntityGraph(attributePaths = {"family", "subscription", "subscription.member"})
    Optional<FamilySubscriptionEntity> findBySubscriptionMemberId(Long memberId);
}
