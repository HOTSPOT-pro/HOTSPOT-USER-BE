package hotspot.user.policy.infrastructure;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;

public interface AppBlockedServiceJpaRepository extends JpaRepository<AppBlockedServiceEntity, Long> {
    long countByAppBlockedServiceIdIn(Set<Long> ids);

    List<AppBlockedServiceEntity> findByIsActiveTrue();

    // 관리자가 생성한 앱 차단 서비스 중 활성화된 것만 가져오기
    @Query("""
        select a
        from AppBlockedServiceEntity a
        where a.appBlockedServiceId in :ids
          and a.isActive = true
    """)
    List<AppBlockedServiceEntity> findByIdInAndIsActiveTrue(
            @Param("ids") List<Long> ids
    );
}
