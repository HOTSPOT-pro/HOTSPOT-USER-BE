package hotspot.user.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {
}
