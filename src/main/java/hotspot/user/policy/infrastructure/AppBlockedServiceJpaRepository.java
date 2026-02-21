package hotspot.user.policy.infrastructure;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {
    long countByAppBlockedServiceIdIn(Set<Long> ids);
}
