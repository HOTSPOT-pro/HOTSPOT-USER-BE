package hotspot.user.policy.infrastructure;

import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {
}
