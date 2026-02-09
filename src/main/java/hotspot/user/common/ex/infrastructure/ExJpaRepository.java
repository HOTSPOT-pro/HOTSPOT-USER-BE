package hotspot.user.common.ex.infrastructure;

import hotspot.user.common.ex.infrastructure.entity.ExEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExJpaRepository extends JpaRepository<ExEntity, Long> {
}
