package hotspot.user.policy.infrastructure;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {
    long countByAppBlockedServiceIdIn(Set<Long> ids);

    @Query("""
        select a
        from AppBlockedServiceEntity a
        where a.appBlockedServiceId in :ids
          and a.isDeleted = false
    """)
    List<AppBlockedServiceEntity> findByIdInAndIsDeletedFalse(
            @Param("ids") List<Long> ids
    );
}
