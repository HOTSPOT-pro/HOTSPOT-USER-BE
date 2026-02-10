package hotspot.user.ex.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.ex.infrastructure.entity.ExEntity;

public interface ExJpaRepository extends JpaRepository<ExEntity, Long> {}
